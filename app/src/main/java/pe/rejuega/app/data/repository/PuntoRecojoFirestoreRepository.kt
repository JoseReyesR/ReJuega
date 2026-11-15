// (nuevo) Repository de puntos fijos y eventos de recojo (HU-04, HU-14).
package pe.rejuega.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pe.rejuega.app.data.remote.PickupEventFirestore
import pe.rejuega.app.data.remote.PuntoEntregaFirestore

class PuntoRecojoFirestoreRepository(
    private val firestore: FirebaseFirestore
) {
    private val puntosCol = firestore.collection("deliveryPoints")
    private val eventosCol = firestore.collection("pickupEvents")

    // (nuevo) Todos los puntos fijos (Admin ve también inactivos).
    fun observarPuntos(): Flow<List<PuntoEntregaFirestore>> = callbackFlow {
        val listener = puntosCol.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("Error observando puntos: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            val lista = snapshot?.documents?.mapNotNull {
                it.toObject(PuntoEntregaFirestore::class.java)?.copy(id = it.id)
            } ?: emptyList()
            trySend(lista)
        }
        awaitClose { listener.remove() }
    }

    // (nuevo) Solo puntos activos (para donante/mapa).
    fun observarPuntosActivos(): Flow<List<PuntoEntregaFirestore>> = callbackFlow {
        val listener = puntosCol.whereEqualTo("activo", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error observando puntos activos: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents?.mapNotNull {
                    it.toObject(PuntoEntregaFirestore::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }

    // (nuevo) Todos los eventos de recojo.
    fun observarEventos(): Flow<List<PickupEventFirestore>> = callbackFlow {
        val listener = eventosCol.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("Error observando eventos: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            val lista = snapshot?.documents?.mapNotNull {
                it.toObject(PickupEventFirestore::class.java)?.copy(id = it.id)
            } ?: emptyList()
            trySend(lista)
        }
        awaitClose { listener.remove() }
    }

    // (nuevo) Eventos activos de una campaña (vista del donante).
    fun observarEventosPorCampaign(campaignId: String): Flow<List<PickupEventFirestore>> = callbackFlow {
        val listener = eventosCol
            .whereEqualTo("campaignId", campaignId)
            .whereEqualTo("activo", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error observando eventos por campaña: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents?.mapNotNull {
                    it.toObject(PickupEventFirestore::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }

    // (nuevo) Crear o actualizar punto fijo.
    suspend fun guardarPunto(punto: PuntoEntregaFirestore) {
        if (punto.id.isBlank()) puntosCol.add(punto).await()
        else puntosCol.document(punto.id).set(punto).await()
    }

    // (nuevo) Activar/desactivar punto fijo (baja lógica).
    suspend fun cambiarEstadoPunto(id: String, activo: Boolean) {
        puntosCol.document(id).update("activo", activo).await()
    }

    // (nuevo) Crear o actualizar evento de recojo.
    suspend fun guardarEvento(evento: PickupEventFirestore) {
        if (evento.id.isBlank()) eventosCol.add(evento).await()
        else eventosCol.document(evento.id).set(evento).await()
    }

    // (nuevo) Activar/desactivar evento.
    suspend fun cambiarEstadoEvento(id: String, activo: Boolean) {
        eventosCol.document(id).update("activo", activo).await()
    }
}
