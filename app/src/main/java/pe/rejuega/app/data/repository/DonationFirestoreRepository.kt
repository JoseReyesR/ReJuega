// (nuevo) Repository para consultar y actualizar donaciones en Node (primario) y Firestore (respaldo).
package pe.rejuega.app.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import pe.rejuega.app.data.remote.Donacion
import pe.rejuega.app.data.remote.DonationNodeDto
import pe.rejuega.app.data.remote.RetrofitClient

class DonationFirestoreRepository(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("donations")
    private val node = RetrofitClient.getNodeApiService()

    // (modificado) Mapeos entre Node DTO y Donacion de la app.
    private fun DonationNodeDto.aFirestore(): Donacion = Donacion(
        id = id, uid = userId, campaignId = 0, campaignTitulo = "",
        nombre = nombreJuguete, descripcion = descripcion ?: "",
        estado = condicion, fotoUrl = fotoUrl,
        estadoTrazabilidad = estadoTrazabilidad, motivoRechazo = motivoRechazo,
        materialTipo = materialTipo ?: "", co2AhorradoKg = co2AhorradoKg
    )

    private fun Donacion.aNode(): DonationNodeDto = DonationNodeDto(
        id = id, userId = uid, campaignId = campaignId.toString(),
        nombreJuguete = nombre, descripcion = descripcion, condicion = estado,
        fotoUrl = fotoUrl, estadoTrazabilidad = estadoTrazabilidad,
        motivoRechazo = motivoRechazo, materialTipo = materialTipo,
        co2AhorradoKg = co2AhorradoKg
    )

    // (modificado) Lectura: Node primario, Firestore respaldo.
    fun obtenerTodas(): Flow<List<Donacion>> = flow {
        emit(obtenerTodasUnaVez())
    }

    suspend fun obtenerTodasUnaVez(): List<Donacion> {
        return try {
            val resp = node.obtenerDonaciones()
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            resp.body()?.data?.map { it.aFirestore() } ?: emptyList()
        } catch (e: Exception) {
            // (respaldo) Firestore.
            try {
                collection.get().await().documents.mapNotNull {
                    it.toObject(Donacion::class.java)?.copy(id = it.id)
                }
            } catch (e2: Exception) { emptyList() }
        }
    }

    // (modificado) Actualizar estado: Node primario + copia en Firestore.
    suspend fun actualizarEstado(docId: String, nuevoEstado: String, cambiadoPor: String) {
        try {
            val resp = node.cambiarEstadoDonacion(
                docId,
                mapOf("estado_trazabilidad" to nuevoEstado, "cambiado_por" to cambiadoPor)
            )
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            println(">>> DONATION UPDATE OK: $docId → $nuevoEstado")
            // (respaldo) Firestore.
            try {
                collection.document(docId).update(
                    mapOf(
                        "estadoTrazabilidad" to nuevoEstado,
                        "historialEstados" to FieldValue.arrayUnion(
                            mapOf("estado" to nuevoEstado, "fecha" to System.currentTimeMillis(), "cambiadoPor" to cambiadoPor)
                        )
                    )
                ).await()
            } catch (_: Exception) {}
        } catch (e: Exception) {
            println(">>> DONATION UPDATE FAIL: ${e.message} → uso Firestore")
            collection.document(docId).update(
                mapOf(
                    "estadoTrazabilidad" to nuevoEstado,
                    "historialEstados" to FieldValue.arrayUnion(
                        mapOf("estado" to nuevoEstado, "fecha" to System.currentTimeMillis(), "cambiadoPor" to cambiadoPor)
                    )
                )
            ).await()
        }
    }

    // (modificado) Rechazar: Node primario + copia en Firestore.
    suspend fun rechazarDonacion(docId: String, motivo: String, cambiadoPor: String) {
        try {
            val resp = node.cambiarEstadoDonacion(
                docId,
                mapOf(
                    "estado_trazabilidad" to "Rechazado",
                    "motivo_rechazo" to motivo,
                    "cambiado_por" to cambiadoPor
                )
            )
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            println(">>> DONATION REJECT OK: $docId")
            // (respaldo) Firestore.
            try {
                collection.document(docId).update(
                    mapOf("estadoTrazabilidad" to "Rechazado", "motivoRechazo" to motivo)
                ).await()
            } catch (_: Exception) {}
        } catch (e: Exception) {
            println(">>> DONATION REJECT FAIL: ${e.message} → uso Firestore")
            collection.document(docId).update(
                mapOf("estadoTrazabilidad" to "Rechazado", "motivoRechazo" to motivo)
            ).await()
        }
    }

    // (modificado) Por usuario: Node primario, Firestore respaldo.
    fun obtenerPorUsuario(uid: String): Flow<List<Donacion>> = flow {
        try {
            val resp = node.obtenerDonaciones(userId = uid)
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            emit(resp.body()?.data?.map { it.aFirestore() } ?: emptyList())
        } catch (e: Exception) {
            // (respaldo) Firestore.
            try {
                val snapshot = collection.whereEqualTo("uid", uid).get().await()
                emit(snapshot.documents.mapNotNull {
                    it.toObject(Donacion::class.java)?.copy(id = it.id)
                })
            } catch (e2: Exception) { emit(emptyList()) }
        }
    }

    // (nuevo) Consulta única para calcular el CO2 total de las donaciones del usuario.
    suspend fun obtenerPorUsuarioUnaVez(uid: String): List<Donacion> {
        return try {
            val resp = node.obtenerDonaciones(userId = uid)
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            resp.body()?.data?.map { it.aFirestore() } ?: emptyList()
        } catch (e: Exception) {
            try {
                val snapshot = collection.whereEqualTo("uid", uid).get().await()
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Donacion::class.java)?.copy(id = doc.id)
                }
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }
}
