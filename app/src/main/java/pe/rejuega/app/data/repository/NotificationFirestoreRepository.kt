// (nuevo) Repository de notificaciones (HU-08).
package pe.rejuega.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
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
    // (modificado) Se ordena en Kotlin para no exigir un índice compuesto en Firestore.
    fun observarPorUsuario(uid: String): Flow<List<Notificacion>> = callbackFlow {
        val listener = collection
            .whereEqualTo("uidDestinatario", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error observando notificaciones: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Notificacion::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.fechaCreacion } ?: emptyList()
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
                if (error != null) {
                    println("Error contando no leídas: ${error.message}")
                    trySend(0)
                    return@addSnapshotListener
                }
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