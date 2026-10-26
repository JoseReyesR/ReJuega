// (modificado) Archivo: MainActivity.kt
// (modificado) Descripción: Activity principal de la app.
// (modificado) Cambio: Se delega la creación de ViewModels a AppNavigation mediante factories.
package pe.rejuega.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import pe.rejuega.app.ui.navigation.AppNavigation
import pe.rejuega.app.ui.theme.ReJuegaTheme

// (modificado) MainActivity simplificada.
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // (modificado) Aplicamos el tema Material 3 de ReJuega.
            ReJuegaTheme {

                // (modificado) Obtenemos el contenedor de dependencias desde la Application.
                val appContainer = (application as ReJuegaApp).container

                // (modificado) Controlador de navegación.
                val navController = rememberNavController()

                // (modificado) Renderizamos la navegación principal.
                AppNavigation(
                    navController = navController,
                    appContainer = appContainer
                )

                // (eliminado) Antes se creaba aquí directamente CampaignViewModel.
                // (eliminado) Ahora cada pantalla crea su ViewModel usando factories en AppNavigation.
            }
        }
    }
}