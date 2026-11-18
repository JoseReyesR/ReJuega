// (nuevo) Repository de campañas sobre Node (primario) y Firestore (respaldo).
package pe.rejuega.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import pe.rejuega.app.data.local.Campaign
import pe.rejuega.app.data.remote.CampaignFirestore
import pe.rejuega.app.data.remote.CampaignNodeDto
import pe.rejuega.app.data.remote.RetrofitClient
import java.io.IOException
import java.util.UUID

// (nuevo) Gestiona el CRUD de campañas en Node.js y Firestore.
class CampaignFirestoreRepository(
    private val firestore: FirebaseFirestore
) {

    // (nuevo) Referencia a la colección campaigns.
    private val collection = firestore.collection("campaigns")

    // (nuevo) Cliente del servidor propio.
    private val node = RetrofitClient.getNodeApiService()

    // (modificado) Lectura: Node primario, Firestore respaldo.
    fun obtenerCampaigns(): Flow<List<CampaignFirestore>> = flow {
        emit(obtenerCampaignsUnaVez())
    }

    suspend fun obtenerCampaignsUnaVez(): List<CampaignFirestore> {
        return try {
            val resp = node.obtenerCampaigns()
            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code()}")
            resp.body()?.data?.map { it.aFirestore() } ?: emptyList()
        } catch (e: Exception) {
            // (respaldo) Firestore como antes.
            try {
                collection.get().await().documents.mapNotNull {
                    it.toObject(CampaignFirestore::class.java)?.copy(id = it.id)
                }
            } catch (e2: Exception) { emptyList() }
        }
    }

    // (modificado) Detalle: Node primario, Firestore respaldo.
    suspend fun obtenerPorId(id: String): CampaignFirestore? {
        return try {
            val resp = node.obtenerCampaignPorId(id)
            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code()}")
            resp.body()?.data?.aFirestore()
        } catch (e: Exception) {
            try {
                collection.document(id).get().await()
                    .toObject(CampaignFirestore::class.java)?.copy(id = id)
            } catch (e2: Exception) { null }
        }
    }

    // (modificado) Escritura: Node primario + copia de respaldo en Firestore.
    suspend fun crearCampaign(campaign: CampaignFirestore) {
        val id = campaign.id.ifBlank { UUID.randomUUID().toString() }
        try {
            val resp = node.crearCampaign(campaign.aNode(id))
            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code()}")
            // (respaldo) Copia best-effort en Firestore.
            try { collection.document(id).set(campaign.copy(id = id)).await() } catch (_: Exception) {}
        } catch (e: Exception) {
            // (comportamiento viejo) Si Node falla, Firestore como antes.
            collection.document(id).set(campaign.copy(id = id)).await()
        }
    }

    // (modificado) Con logs de diagnóstico para ver por qué falla el PUT.
    suspend fun actualizarCampaign(campaign: CampaignFirestore) {
        try {
            val resp = node.actualizarCampaign(campaign.id, campaign.aNode(campaign.id))
            if (!resp.isSuccessful) {
                // (nuevo) Muestra el código y el mensaje exacto del servidor.
                println(">>> CAMPAIGN PUT FAIL: HTTP ${resp.code()} | ${resp.errorBody()?.string()}")
                throw IOException("HTTP ${resp.code()}")
            }
            println(">>> CAMPAIGN PUT OK: ${campaign.id}")
            // (respaldo) Copia best-effort en Firestore.
            try { collection.document(campaign.id).set(campaign).await() } catch (_: Exception) {}
        } catch (e: Exception) {
            println(">>> CAMPAIGN PUT EX: ${e.message} → uso respaldo Firestore")
            collection.document(campaign.id).set(campaign).await()
        }
    }

    suspend fun cerrarCampaign(id: String) {
        try {
            val resp = node.cambiarEstadoCampaign(id, mapOf("estado" to "Cerrada"))
            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code()}")
            try { collection.document(id).update("estado", "Cerrada").await() } catch (_: Exception) {}
        } catch (e: Exception) {
            collection.document(id).update("estado", "Cerrada").await()
        }
    }

    // (nuevo) Elimina campaña: Node primario + borrado en Firestore como respaldo.
    suspend fun eliminarCampaign(id: String) {
        try {
            val resp = node.eliminarCampaign(id)
            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code()}")
            println(">>> CAMPAIGN DELETE OK: $id")
            // (respaldo) Borrar también en Firestore.
            try { collection.document(id).delete().await() } catch (_: Exception) {}
        } catch (e: Exception) {
            println(">>> CAMPAIGN DELETE EX: ${e.message} → uso Firestore")
            collection.document(id).delete().await()
        }
    }

    // (nuevo) Inserta campañas semilla en Firestore solo si la colección está vacía.
    suspend fun inicializarCampaignsSemilla(campaignsSeed: List<Campaign>) {
        val snapshot = collection.limit(1).get().await()
        if (!snapshot.isEmpty) {
            return
        }
        campaignsSeed.forEach { seed ->
            val campaign = CampaignFirestore(
                titulo = seed.titulo,
                descripcion = seed.descripcion,
                fechaInicio = seed.fechaInicio,
                fechaFin = seed.fechaFin,
                estado = seed.estado,
                imagenUrl = seed.imagenUrl,
                creadoPor = "seed",
                fechaCreacion = System.currentTimeMillis()
            )
            collection.add(campaign).await()
        }
    }
}

// (nuevo) Mapeos entre Node DTO y modelo de la app.
private fun CampaignNodeDto.aFirestore(): CampaignFirestore = CampaignFirestore(
    id = id, titulo = titulo, descripcion = descripcion ?: "",
    fechaInicio = fechaInicio, fechaFin = fechaFin, estado = estado,
    imagenUrl = imagenUrl ?: "", creadoPor = creadoPor ?: ""
)

private fun CampaignFirestore.aNode(conId: String): CampaignNodeDto = CampaignNodeDto(
    id = conId, titulo = titulo, descripcion = descripcion,
    fechaInicio = fechaInicio, fechaFin = fechaFin, estado = estado,
    imagenUrl = imagenUrl, creadoPor = creadoPor
)
