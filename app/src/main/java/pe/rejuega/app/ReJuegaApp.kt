// (modificado) Archivo: ReJuegaApp.kt
// (modificado) Descripción: Clase Application que inicializa dependencias y datos semilla.
// (modificado) Cambio: Se agrega JugueteRepository y lanzamiento de inicialización asíncrona.
package pe.rejuega.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pe.rejuega.app.data.local.ReJuegaDatabase
import pe.rejuega.app.data.repository.CampaignRepository
import pe.rejuega.app.data.repository.JugueteRepository
import pe.rejuega.app.di.AppContainer

// (modificado) Clase Application personalizada.
class ReJuegaApp : Application() {

    // (modificado) Contenedor global de dependencias.
    lateinit var container: AppContainer

    // (nuevo) Scope de corrutinas para tareas de inicialización de la app.
    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        // (modificado) Inicializamos la base de datos Room.
        val database = ReJuegaDatabase.getDatabase(this)

        // (modificado) Creamos repositorios.
        val campaignRepository = CampaignRepository(database.campaignDao())
        val jugueteRepository = JugueteRepository(database.jugueteDao())

        // (modificado) Guardamos dependencias en el contenedor.
        container = AppContainer(
            campaignRepository = campaignRepository,
            jugueteRepository = jugueteRepository
        )

        // (nuevo) Insertamos datos hardcodeados al iniciar la app.
        inicializarDatosSemilla()
    }

    // (nuevo) Función para cargar datos semilla en segundo plano.
    private fun inicializarDatosSemilla() {
        applicationScope.launch {
            try {
                container.campaignRepository.inicializarDatosSemilla()
                container.jugueteRepository.inicializarDatosSemilla()
            } catch (e: Exception) {
                println("Error inicializando datos semilla: ${e.message}")
            }
        }
    }
}