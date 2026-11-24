// (nuevo) Repository de puntos fijos y eventos de recojo en Node (primario) y Firestore (respaldo).
package pe.rejuega.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import pe.rejuega.app.data.remote.DeliveryPointNodeDto
import pe.rejuega.app.data.remote.PickupEventFirestore
import pe.rejuega.app.data.remote.PickupEventNodeDto
import pe.rejuega.app.data.remote.PuntoEntregaFirestore
import pe.rejuega.app.data.remote.RetrofitClient
import java.util.UUID

class PuntoRecojoFirestoreRepository(
    private val firestore: FirebaseFirestore
) {
    private val puntosCol = firestore.collection("deliveryPoints")
    private val eventosCol = firestore.collection("pickupEvents")
    private val node = RetrofitClient.getNodeApiService()

    // (modificado) Mapeos entre Node DTO y modelos de la app.
    private fun DeliveryPointNodeDto.aFirestore(): PuntoEntregaFirestore = PuntoEntregaFirestore(
        id = id, nombre = nombre, direccion = direccion, horario = horario ?: "",
        dias = diasAtencion?.split(",") ?: emptyList(),
        horaInicio = horaInicio ?: "", horaFin = horaFin ?: "",
        latitud = latitud, longitud = longitud, activo = activo, creadoPor = creadoPor ?: ""
    )

    private fun PuntoEntregaFirestore.aNode(): DeliveryPointNodeDto = DeliveryPointNodeDto(
        id = id, nombre = nombre, direccion = direccion, horario = horario,
        diasAtencion = dias.joinToString(","), horaInicio = horaInicio, horaFin = horaFin,
        latitud = latitud, longitud = longitud, activo = activo, creadoPor = creadoPor
    )

    private fun PickupEventNodeDto.aFirestore(): PickupEventFirestore = PickupEventFirestore(
        id = id, campaignId = campaignId, nombre = nombre, lugar = lugar ?: "",
        direccion = direccion ?: "", fecha = fecha, hora = hora,
        latitud = latitud, longitud = longitud, activo = activo, creadoPor = creadoPor ?: ""
    )

    private fun PickupEventFirestore.aNode(): PickupEventNodeDto = PickupEventNodeDto(
        id = id, campaignId = campaignId, nombre = nombre, lugar = lugar,
        direccion = direccion, fecha = fecha, hora = hora,
        latitud = latitud, longitud = longitud, activo = activo, creadoPor = creadoPor
    )

    // (modificado) Todos los puntos (Admin ve también inactivos).
    fun observarPuntos(): Flow<List<PuntoEntregaFirestore>> = flow {
        try {
            val resp = node.obtenerPuntosEntrega()
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            emit(resp.body()?.data?.map { it.aFirestore() } ?: emptyList())
        } catch (e: Exception) {
            try {
                val snapshot = puntosCol.get().await()
                emit(snapshot.documents.mapNotNull {
                    it.toObject(PuntoEntregaFirestore::class.java)?.copy(id = it.id)
                })
            } catch (e2: Exception) { emit(emptyList()) }
        }
    }

    // (modificado) Solo puntos activos (para donante/mapa).
    fun observarPuntosActivos(): Flow<List<PuntoEntregaFirestore>> = flow {
        try {
            val resp = node.obtenerPuntosEntrega()
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            emit(resp.body()?.data?.filter { it.activo }?.map { it.aFirestore() } ?: emptyList())
        } catch (e: Exception) {
            try {
                val snapshot = puntosCol.whereEqualTo("activo", true).get().await()
                emit(snapshot.documents.mapNotNull {
                    it.toObject(PuntoEntregaFirestore::class.java)?.copy(id = it.id)
                })
            } catch (e2: Exception) { emit(emptyList()) }
        }
    }

    // (modificado) Todos los eventos de recojo.
    fun observarEventos(): Flow<List<PickupEventFirestore>> = flow {
        try {
            val resp = node.obtenerEventosRecojo()
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            emit(resp.body()?.data?.map { it.aFirestore() } ?: emptyList())
        } catch (e: Exception) {
            try {
                val snapshot = eventosCol.get().await()
                emit(snapshot.documents.mapNotNull {
                    it.toObject(PickupEventFirestore::class.java)?.copy(id = it.id)
                })
            } catch (e2: Exception) { emit(emptyList()) }
        }
    }

    // (modificado) Eventos activos de una campaña (vista del donante).
    fun observarEventosPorCampaign(campaignId: String): Flow<List<PickupEventFirestore>> = flow {
        try {
            val resp = node.obtenerEventosRecojo(campaignId = campaignId)
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            emit(resp.body()?.data?.filter { it.activo }?.map { it.aFirestore() } ?: emptyList())
        } catch (e: Exception) {
            try {
                val snapshot = eventosCol
                    .whereEqualTo("campaignId", campaignId)
                    .whereEqualTo("activo", true).get().await()
                emit(snapshot.documents.mapNotNull {
                    it.toObject(PickupEventFirestore::class.java)?.copy(id = it.id)
                })
            } catch (e2: Exception) { emit(emptyList()) }
        }
    }

    // (modificado) Crear o actualizar punto fijo.
    suspend fun guardarPunto(punto: PuntoEntregaFirestore) {
        val id = punto.id.ifBlank { UUID.randomUUID().toString() }
        val puntoConId = punto.copy(id = id)
        try {
            val resp = if (punto.id.isBlank()) node.crearPuntoEntrega(puntoConId.aNode()) else node.actualizarPuntoEntrega(id, puntoConId.aNode())
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            println(">>> POINT SAVE OK: $id")
            try { puntosCol.document(id).set(puntoConId).await() } catch (_: Exception) {}
        } catch (e: Exception) {
            println(">>> POINT SAVE FAIL: ${e.message} → uso Firestore")
            puntosCol.document(id).set(puntoConId).await()
        }
    }

    // (modificado) Crear punto directo.
    suspend fun crearPunto(punto: PuntoEntregaFirestore) {
        guardarPunto(punto)
    }

    // (nuevo) Activar/desactivar punto fijo.
    suspend fun cambiarEstadoPunto(id: String, activo: Boolean) {
        try {
            puntosCol.document(id).update("activo", activo).await()
        } catch (_: Exception) {}
    }

    // (modificado) Crear o actualizar evento de recojo.
    suspend fun guardarEvento(evento: PickupEventFirestore) {
        val id = evento.id.ifBlank { UUID.randomUUID().toString() }
        val eventoConId = evento.copy(id = id)
        try {
            val resp = if (evento.id.isBlank()) node.crearEventoRecojo(eventoConId.aNode()) else node.actualizarEventoRecojo(id, eventoConId.aNode())
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            println(">>> EVENT SAVE OK: $id")
            try { eventosCol.document(id).set(eventoConId).await() } catch (_: Exception) {}
        } catch (e: Exception) {
            println(">>> EVENT SAVE FAIL: ${e.message} → uso Firestore")
            eventosCol.document(id).set(eventoConId).await()
        }
    }

    // (modificado) Crear evento directo.
    suspend fun crearEvento(evento: PickupEventFirestore) {
        guardarEvento(evento)
    }

    // (nuevo) Activar/desactivar evento.
    suspend fun cambiarEstadoEvento(id: String, activo: Boolean) {
        try {
            eventosCol.document(id).update("activo", activo).await()
        } catch (_: Exception) {}
    }
}
