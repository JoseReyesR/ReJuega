// (nuevo) Archivo: ReJuegaApp.kt
// (nuevo) Descripción: Clase Application que se ejecuta una sola vez cuando la app arranca.
// (nuevo) Sirve para inicializar dependencias globales (Base de datos, Repositories).
// (nuevo) Nota: Debe ser registrada en el AndroidManifest.xml para que Android la reconozca.
package pe.rejuega.app

import android.app.Application
import pe.rejuega.app.data.local.ReJuegaDatabase
import pe.rejuega.app.data.repository.CampaignRepository
import pe.rejuega.app.di.AppContainer

// (nuevo) La clase Application es el punto de entrada global de la app.
// (nuevo) Android la crea ANTES de cualquier Activity o pantalla.
class ReJuegaApp : Application() {

    // (nuevo) Contenedor que guardará todas nuestras dependencias (Repositories, DAOs, etc.)
    lateinit var container: AppContainer

    // (nuevo) Este método se llama automáticamente cuando la app arranca por primera vez.
    override fun onCreate() {
        super.onCreate()

        // (nuevo) 1. Inicializamos la base de datos Room usando el contexto de la aplicación.
        val database = ReJuegaDatabase.getDatabase(this)

        // (nuevo) 2. Creamos el Repository pasándole el DAO de campañas.
        val campaignRepository = CampaignRepository(database.campaignDao())

        // (nuevo) 3. Guardamos el Repository en el contenedor para acceder desde cualquier ViewModel.
        container = AppContainer(campaignRepository = campaignRepository)
    }
}