// (nuevo) Repository de notificaciones en Node (primario) y Firestore (respaldo).
package pe.rejuega.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import pe.rejuega.app.data.remote.Notificacion
import pe.rejuega.app.data.remote.NotificationNodeDto
import pe.rejuega.app.data.remote.RetrofitClient
import java.util.UUID

class NotificationFirestoreRepository(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("notifications")
    private val node = RetrofitClient.getNodeApiService()

    // (modificado) Mapeos entre Node DTO y Notificacion de la app.
    private fun NotificationNodeDto.aFirestore(): Notificacion = Notificacion(
        id = id, uidDestinatario = userId, titulo = titulo, mensaje = mensaje,
        donacionId = donationId ?: "", tipo = tipo, leida = leida,
        fechaCreacion = fechaCreacion?.toLongOrNull() ?: 0
    )

    private fun Notificacion.aNode(): NotificationNodeDto = NotificationNodeDto(
        id = id, userId = uidDestinatario, titulo = titulo, mensaje = mensaje,
        donationId = donacionId, tipo = tipo, leida = leida
    )

    // (modificado) Observar notificaciones: Node primario, Firestore respaldo.
    fun observarPorUsuario(uid: String): Flow<List<Notificacion>> = flow {
        println(">>> NOTIF READ: solicitando uid=$uid")
        try {
            val resp = node.obtenerNotificaciones(uid)
            println(">>> NOTIF READ: HTTP ${resp.code()} count=${resp.body()?.count}")
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            val lista = resp.body()?.data?.map { it.aFirestore() } ?: emptyList()
            println(">>> NOTIF READ: mapeadas=${lista.size}")
            emit(lista)
        } catch (e: Exception) {
            println(">>> NOTIF READ FAIL: ${e.message} → uso Firestore")
            try {
                val snapshot = collection
                    .whereEqualTo("uidDestinatario", uid)
                    .get().await()
                val lista = snapshot.documents.mapNotNull {
                    it.toObject(Notificacion::class.java)?.copy(id = it.id)
                }
                println(">>> NOTIF READ (Firestore): ${lista.size}")
                emit(lista)
            } catch (e2: Exception) { emit(emptyList()) }
        }
    }

    // (modificado) Contar no leídas: Node primario, Firestore respaldo.
    fun contarNoLeidas(uid: String): Flow<Int> = flow {
        try {
            val resp = node.contarNoLeidas(uid)
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            emit(resp.body()?.data?.get("count") ?: 0)
        } catch (e: Exception) {
            // (respaldo) Firestore.
            try {
                val snapshot = collection
                    .whereEqualTo("uidDestinatario", uid)
                    .whereEqualTo("leida", false)
                    .get().await()
                emit(snapshot.size())
            } catch (e2: Exception) { emit(0) }
        }
    }

    // (modificado) Crear notificación: Node primario + copia en Firestore.
    suspend fun crear(notificacion: Notificacion) {
        val id = notificacion.id.ifBlank { UUID.randomUUID().toString() }
        try {
            val resp = node.crearNotificacion(notificacion.copy(id = id).aNode())
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            println(">>> NOTIF CREATE OK: $id")
            try { collection.document(id).set(notificacion.copy(id = id)).await() } catch (_: Exception) {}
        } catch (e: Exception) {
            println(">>> NOTIF CREATE FAIL: ${e.message} → uso Firestore")
            firestore.collection("notifications").document(id).set(notificacion.copy(id = id)).await()
        }
    }

    // (modificado) Marcar leída: Node primario + copia en Firestore.
    suspend fun marcarLeida(id: String) {
        try {
            val resp = node.marcarNotificacionLeida(id)
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            println(">>> NOTIF READ OK: $id")
            try { collection.document(id).update("leida", true).await() } catch (_: Exception) {}
        } catch (e: Exception) {
            println(">>> NOTIF READ FAIL: ${e.message} → uso Firestore")
            collection.document(id).update("leida", true).await()
        }
    }

    // (modificado) Marcar todas leídas: Node primario + copia en Firestore.
    suspend fun marcarTodasLeidas(uid: String) {
        try {
            val resp = node.marcarTodasLeidas(uid)
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            println(">>> NOTIF READ-ALL OK: $uid")
            try {
                val snapshot = collection
                    .whereEqualTo("uidDestinatario", uid)
                    .whereEqualTo("leida", false).get().await()
                snapshot.documents.forEach {
                    collection.document(it.id).update("leida", true).await()
                }
            } catch (_: Exception) {}
        } catch (e: Exception) {
            println(">>> NOTIF READ-ALL FAIL: ${e.message} → uso Firestore")
            val snapshot = collection
                .whereEqualTo("uidDestinatario", uid)
                .whereEqualTo("leida", false).get().await()
            snapshot.documents.forEach {
                collection.document(it.id).update("leida", true).await()
            }
        }
    }
}
