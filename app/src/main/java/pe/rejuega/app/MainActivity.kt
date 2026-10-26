// (modificado) Archivo: MainActivity.kt
// (modificado) Descripción: Activity principal que arranca la app y configura la navegación.
// (modificado) Cambio: Se reemplaza el contenido por defecto ("Hello Android") por el NavHost de ReJuega.
package pe.rejuega.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import pe.rejuega.app.ui.navigation.AppNavigation
import pe.rejuega.app.ui.theme.ReJuegaTheme
import pe.rejuega.app.ui.viewmodel.CampaignViewModel
import pe.rejuega.app.ui.viewmodel.CampaignViewModelFactory

// (modificado) La Activity principal es el punto de entrada visual de la app.
// (modificado) En Compose, una sola Activity puede contener todas las pantallas (navegación interna).
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // (modificado) setContent define el contenido visual de la Activity usando Jetpack Compose.
        setContent {
            // (modificado) Aplicamos el tema Material 3 personalizado de ReJuega (verde y naranja).
            ReJuegaTheme {

                // (nuevo) Obtenemos el contenedor de dependencias desde la clase Application.
                // (nuevo) Este contenedor fue inicializado en ReJuegaApp.onCreate().
                val appContainer = (application as ReJuegaApp).container

                // (nuevo) Creamos el ViewModel de campañas usando la Factory que inyecta el Repository.
                // (nuevo) ViewModelProvider se encarga de crear o recuperar el ViewModel según el ciclo de vida.
                val campaignViewModel = ViewModelProvider(
                    this,
                    CampaignViewModelFactory(appContainer.campaignRepository)
                )[CampaignViewModel::class.java]

                // (nuevo) Creamos el controlador de navegación.
                // (nuevo) rememberNavController guarda el estado de la navegación entre recomposiciones.
                val navController = rememberNavController()

                // (nuevo) Renderizamos el NavHost que contiene todas las pantallas de la app.
                AppNavigation(
                    navController = navController,
                    campaignViewModel = campaignViewModel
                )
            }
        }
    }
}