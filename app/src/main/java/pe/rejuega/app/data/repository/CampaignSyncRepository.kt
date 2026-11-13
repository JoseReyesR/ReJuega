// (nuevo) Descripción: Repository híbrido que sincroniza campañas entre Firestore y Room.
// (nuevo) Estrategia: Cache-First, Network-Refresh (RB-05, RN-02).
package pe.rejuega.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import pe.rejuega.app.data.local.Campaign
import pe.rejuega.app.data.local.CampaignDao
import pe.rejuega.app.data.remote.CampaignFirestore

class CampaignSyncRepository(
    private val campaignDao: CampaignDao,
    private val firestore: FirebaseFirestore
) {

    // (nuevo) Obtiene campañas con estrategia Cache-First, Network-Refresh.
    // (nuevo) Emite primero el caché (Room), luego intenta sincronizar con Firestore.
    fun obtenerCampaigns(): Flow<List<Campaign>> = flow {
        // (nuevo) PASO 1: Emitir caché inmediatamente (respuesta rápida).
        val cached = campaignDao.obtenerCampaignsSync()
        emit(cached)

        // (nuevo) PASO 2: Intentar sincronizar con Firestore.
        try {
            val snapshot = firestore.collection("campaigns").get().await()
            val remoteCampaigns = snapshot.documents.mapNotNull { doc ->
                doc.toObject(CampaignFirestore::class.java)?.let { cf ->
                    // (nuevo) Mapeo Firestore -> Room.
                    Campaign(
                        firestoreId = doc.id,
                        titulo = cf.titulo,
                        descripcion = cf.descripcion,
                        fechaInicio = cf.fechaInicio,
                        fechaFin = cf.fechaFin,
                        estado = cf.estado,
                        imagenUrl = cf.imagenUrl,
                        ultimaSincronizacion = System.currentTimeMillis()
                    )
                }
            }

            // (nuevo) PASO 3: Solo actualizar caché si hay datos remotos.
            // (nuevo) Si Firestore está vacío, mantenemos el caché existente.
            if (remoteCampaigns.isNotEmpty()) {
                campaignDao.borrarTodas()
                campaignDao.insertarCampaigns(remoteCampaigns)
                // (nuevo) Emitir datos actualizados.
                emit(campaignDao.obtenerCampaignsSync())
            }
        } catch (e: Exception) {
            // (nuevo) Sin conexión o error: ya emitimos el caché, no hacemos nada.
            // (nuevo) La UI mostrará los datos cacheados con un indicador offline.
        }
    }

    // (nuevo) Fuerza una sincronización completa (para botón de refrescar).
    // (nuevo) Lanza excepción si no hay conexión, para que el ViewModel la maneje.
    suspend fun sincronizar() {
        val snapshot = firestore.collection("campaigns").get().await()
        val remoteCampaigns = snapshot.documents.mapNotNull { doc ->
            doc.toObject(CampaignFirestore::class.java)?.let { cf ->
                Campaign(
                    firestoreId = doc.id,
                    titulo = cf.titulo,
                    descripcion = cf.descripcion,
                    fechaInicio = cf.fechaInicio,
                    fechaFin = cf.fechaFin,
                    estado = cf.estado,
                    imagenUrl = cf.imagenUrl,
                    ultimaSincronizacion = System.currentTimeMillis()
                )
            }
        }
        if (remoteCampaigns.isNotEmpty()) {
            campaignDao.borrarTodas()
            campaignDao.insertarCampaigns(remoteCampaigns)
        }
    }
}