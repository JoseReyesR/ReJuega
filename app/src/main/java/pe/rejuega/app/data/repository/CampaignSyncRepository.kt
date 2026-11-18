// (nuevo) Descripción: Repository híbrido que sincroniza campañas entre Node/Firestore y Room.
// (nuevo) Estrategia: Cache-First, Network-Refresh (RB-05, RN-02).
package pe.rejuega.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import pe.rejuega.app.data.local.Campaign
import pe.rejuega.app.data.local.CampaignDao
import pe.rejuega.app.data.remote.CampaignFirestore
import pe.rejuega.app.data.remote.RetrofitClient
import java.io.IOException

class CampaignSyncRepository(
    private val campaignDao: CampaignDao,
    private val firestore: FirebaseFirestore
) {

    // (nuevo) Lectura remota: Node primario, Firestore respaldo.
    private suspend fun obtenerRemotas(): List<Campaign> {
        return try {
            val resp = RetrofitClient.getNodeApiService().obtenerCampaigns()
            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code()}")
            resp.body()?.data?.map { n ->
                Campaign(
                    firestoreId = n.id,
                    titulo = n.titulo,
                    descripcion = n.descripcion ?: "",
                    fechaInicio = n.fechaInicio,
                    fechaFin = n.fechaFin,
                    estado = n.estado,
                    imagenUrl = n.imagenUrl ?: "",
                    ultimaSincronizacion = System.currentTimeMillis()
                )
            } ?: emptyList()
        } catch (e: Exception) {
            // (respaldo) Firestore como antes.
            val snapshot = firestore.collection("campaigns").get().await()
            snapshot.documents.mapNotNull { doc ->
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
        }
    }

    // (nuevo) Obtiene campañas con estrategia Cache-First, Network-Refresh.
    fun obtenerCampaigns(): Flow<List<Campaign>> = flow {
        // (nuevo) PASO 1: Emitir caché inmediatamente (respuesta rápida).
        val cached = campaignDao.obtenerCampaignsSync()
        emit(cached)

        // (nuevo) PASO 2: Intentar sincronizar con el servidor remoto (Node/Firestore).
        try {
            val remoteCampaigns = obtenerRemotas()

            // (nuevo) PASO 3: Actualizar caché si hay datos remotos.
            if (remoteCampaigns.isNotEmpty()) {
                campaignDao.borrarTodas()
                campaignDao.insertarCampaigns(remoteCampaigns)
                emit(campaignDao.obtenerCampaignsSync())
            }
        } catch (e: Exception) {
            // (nuevo) Sin conexión o error: ya emitimos el caché, no hacemos nada.
        }
    }

    // (nuevo) Fuerza una sincronización completa (para botón de refrescar).
    suspend fun sincronizar() {
        val remoteCampaigns = obtenerRemotas()
        if (remoteCampaigns.isNotEmpty()) {
            campaignDao.borrarTodas()
            campaignDao.insertarCampaigns(remoteCampaigns)
        }
    }
}
