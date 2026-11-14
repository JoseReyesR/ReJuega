// (nuevo) Repository para consultar y actualizar donaciones en Firestore.
package pe.rejuega.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pe.rejuega.app.data.remote.Donacion
import com.google.firebase.firestore.FieldValue
class DonationFirestoreRepository(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("donations")

    // (modificado) obtenerTodas(): ahora conserva el ID del documento Firestore.
    fun obtenerTodas(): Flow<List<Donacion>> = callbackFlow {
        val listener = collection
            .orderBy("fechaRegistro", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val lista = snapshot?.documents?.mapNotNull { doc ->
                    // (modificado) Antes: .copy() → perdía el docId.
                    // (modificado) Ahora: .copy(id = doc.id) → cada Donacion conoce su documento.
                    doc.toObject(Donacion::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }

    // (nuevo) Escucha solo las donaciones de un usuario específico (por UID).
    fun obtenerPorUsuario(uid: String): Flow<List<Donacion>> = callbackFlow {
        val listener = collection
            .whereEqualTo("uid", uid)
            .orderBy("fechaRegistro", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val lista = snapshot?.documents?.mapNotNull { doc ->
                    //doc.toObject(Donacion::class.java)?.copy()
                    doc.toObject(Donacion::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }

    // (modificado) Avanza el estado y agrega el hito al historial con fecha y autor.
    suspend fun actualizarEstado(docId: String, nuevoEstado: String, cambiadoPor: String) {
        collection.document(docId).update(
            mapOf(
                "estadoTrazabilidad" to nuevoEstado,
                // (nuevo) arrayUnion agrega sin pisar los hitos anteriores (RB-11).
                "historialEstados" to FieldValue.arrayUnion(
                    mapOf(
                        "estado" to nuevoEstado,
                        "fecha" to System.currentTimeMillis(),
                        "cambiadoPor" to cambiadoPor
                    )
                )
            )
        ).await()
    }

    // (modificado) Rechaza con motivo y deja el hito en el historial.
    suspend fun rechazarDonacion(docId: String, motivo: String, cambiadoPor: String) {
        collection.document(docId).update(
            mapOf(
                "estadoTrazabilidad" to "Rechazado",
                "motivoRechazo" to motivo,
                "historialEstados" to FieldValue.arrayUnion(
                    mapOf(
                        "estado" to "Rechazado",
                        "fecha" to System.currentTimeMillis(),
                        "cambiadoPor" to cambiadoPor
                    )
                )
            )
        ).await()
    }



    // (nuevo) Observa solo las donaciones del usuario (trazabilidad del donante).
    // (nuevo) Sin orderBy para no exigir índice compuesto.
    fun obtenerPorUsuario(uid: String): Flow<List<Donacion>> = callbackFlow {
        val listener = collection.whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val lista = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Donacion::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }


}