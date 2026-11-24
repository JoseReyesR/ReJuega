// (nuevo) Archivo: PickupEventRepository.kt
// (nuevo) Descripción: Repositorio para los eventos de recojo por campaña.
// (nuevo) Responsabilidad: exponer los eventos de recojo desde Room al ViewModel.
package pe.rejuega.app.data.repository

import kotlinx.coroutines.flow.Flow
import pe.rejuega.app.data.local.PickupEvent
import pe.rejuega.app.data.local.PickupEventDao
import pe.rejuega.app.utils.DatosSemilla

// (nuevo) Repositorio que gestiona los eventos de recojo.
// (nuevo) Recibe el DAO mediante el constructor (inyección de dependencias).
class PickupEventRepository(
    private val pickupEventDao: PickupEventDao
) {

    // (nuevo) Obtiene los eventos de recojo activos como Flow.
    // (nuevo) Se usará en el mapa para mostrar los marcadores (futuro).
    fun obtenerPickupEventsActivos(): Flow<List<PickupEvent>> {
        return pickupEventDao.obtenerPickupEventsActivos()
    }

    // (nuevo) Obtiene los eventos de recojo de una campaña específica.
    // (nuevo) Se usará en el detalle de campaña (futuro).
    fun obtenerPickupEventsPorCampaign(campaignId: Long): Flow<List<PickupEvent>> {
        return pickupEventDao.obtenerPickupEventsPorCampaign(campaignId)
    }

    // (nuevo) Inserta los datos semilla de eventos de recojo.
    // (nuevo) Se llama al iniciar la app para que el mapa tenga datos (futuro).
    suspend fun inicializarDatosSemilla() {
        try {
            pickupEventDao.insertarPickupEventsSeed(DatosSemilla.listaPickupEvents)
        } catch (e: Exception) {
            // (nuevo) Si falla la inserción, se registra pero no se detiene la app.
            println("Error al insertar pickup events semilla: ${e.message}")
        }
    }
}