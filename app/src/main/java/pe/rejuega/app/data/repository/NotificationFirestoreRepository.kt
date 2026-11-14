// (nuevo) Repository de notificaciones (HU-08).
package pe.rejuega.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pe.rejuega.app.data.remote.Notificacion

class NotificationFirestoreRepository(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("notifications")

    // (nuevo) Observa las notificaciones del usuario (listener en tiempo real).
    fun observarPorUsuario(uid: String): Flow<List<Notificacion>> = callbackFlow {
        val listener = collection
            .whereEqualTo("uidDestinatario", uid)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val lista = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Notificacion::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }

    // (nuevo) Cuenta las notificaciones no leídas (para el badge del Home).
    fun contarNoLeidas(uid: String): Flow<Int> = callbackFlow {
        val listener = collection
            .whereEqualTo("uidDestinatario", uid)
            .whereEqualTo("leida", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }

    // (nuevo) Crea una notificación (lo llama el Admin al cambiar estado).
    suspend fun crear(notificacion: Notificacion) {
        collection.add(notificacion).await()
    }

    // (nuevo) Marca una notificación como leída.
    suspend fun marcarLeida(id: String) {
        collection.document(id).update("leida", true).await()
    }

    // (nuevo) Marca todas las del usuario como leídas.
    suspend fun marcarTodasLeidas(uid: String) {
        val snapshot = collection.whereEqualTo("uidDestinatario", uid)
            .whereEqualTo("leida", false).get().await()
        snapshot.documents.forEach {
            collection.document(it.id).update("leida", true).await()
        }
    }
}