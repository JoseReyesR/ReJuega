// (nuevo) Repository de campañas sobre Firestore.
package pe.rejuega.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pe.rejuega.app.data.remote.CampaignFirestore

// (nuevo) Gestiona el CRUD de campañas en Firestore.
class CampaignFirestoreRepository(
    private val firestore: FirebaseFirestore
) {

    // (nuevo) Referencia a la colección campaigns.
    private val collection = firestore.collection("campaigns")

    // (nuevo) Obtiene todas las campañas en tiempo real.
    // (nuevo) Se actualiza automáticamente si el Admin crea/edita una.
    fun obtenerCampaigns(): Flow<List<CampaignFirestore>> = callbackFlow {
        val listener = collection
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                // (nuevo) Mapea documentos a objetos, asignando el ID del documento.
                val campaigns = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CampaignFirestore::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(campaigns)
            }
        awaitClose { listener.remove() }
    }

    // (nuevo) Crea una campaña nueva.
    suspend fun crearCampaign(campaign: CampaignFirestore) {
        collection.add(campaign).await()
    }

    // (nuevo) Actualiza una campaña existente.
    suspend fun actualizarCampaign(campaign: CampaignFirestore) {
        collection.document(campaign.id).set(campaign).await()
    }

    // (nuevo) Cierra una campaña (cambia estado a "Cerrada").
    suspend fun cerrarCampaign(id: String) {
        collection.document(id).update("estado", "Cerrada").await()
    }
}