// (nuevo) Archivo: CampaignRepository.kt
// (nuevo) Descripción: Implementa el patrón Repository para la entidad Campaign.
// (nuevo) Responsabilidad: Abstraer el acceso a datos del ViewModel, facilitando el intercambio entre Room y Firestore en el futuro.
package pe.rejuega.app.data.repository

import kotlinx.coroutines.flow.Flow
import pe.rejuega.app.data.local.Campaign
import pe.rejuega.app.data.local.CampaignDao
import pe.rejuega.app.utils.DatosSemilla

// (nuevo) El repositorio recibe el DAO a través del constructor (Inyección de Dependencias básica)
class CampaignRepository(private val campaignDao: CampaignDao) {

    // (nuevo) Expone el flujo de campañas desde la base de datos local hacia el ViewModel.
    // (nuevo) El ViewModel solo conoce esta propiedad, no conoce los detalles de Room.
    val campaigns: Flow<List<Campaign>> = campaignDao.obtenerCampaigns()

    // (nuevo) Método de inicialización: Carga los datos hardcodeados en la base de datos local.
    // (nuevo) Esto garantiza que la app no esté vacía la primera vez que el usuario la abre.
    // (nuevo) Nota: En producción, esto se reemplazaría por una sincronización con Firestore.
    suspend fun inicializarDatosSemilla() {
        try {
            campaignDao.insertarCampaigns(DatosSemilla.listaCampaigns)
        } catch (e: Exception) {
            // (nuevo) En caso de error al insertar datos semilla, se registra pero no se detiene la app.
            // (nuevo) Esto puede ocurrir si los datos ya existen y hay conflictos de clave primaria.
            println("Error al inicializar datos semilla: ${e.message}")
        }
    }

    // (nuevo) Método para obtener una campaña específica por su ID.
    // (nuevo) Útil para la pantalla de detalle de campaña.
    suspend fun obtenerCampaignPorId(id: Long): Campaign? {
        return campaignDao.obtenerCampaignPorId(id)
    }
}