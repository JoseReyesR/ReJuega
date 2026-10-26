// (nuevo) Archivo: AppNavigation.kt
// (nuevo) Descripción: NavHost que define todas las pantallas de la app y cómo navegar entre ellas.
// (nuevo) Usa la librería Navigation Compose (agregada en el Paso 2).
// (nuevo) Por ahora solo está implementada la pantalla Home. Las demás se agregarán en futuros pasos.
package pe.rejuega.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pe.rejuega.app.ui.screens.HomeScreen
import pe.rejuega.app.ui.viewmodel.CampaignViewModel

// (nuevo) Composable principal de navegación.
// (nuevo) Recibe el NavHostController (que gestiona la pila de pantallas) y el ViewModel de campañas.
@Composable
fun AppNavigation(
    navController: NavHostController,
    campaignViewModel: CampaignViewModel
) {
    // (nuevo) NavHost es el contenedor de todas las pantallas (destinos) de la app.
    // (nuevo) startDestination indica cuál es la primera pantalla que se muestra al abrir la app.
    NavHost(
        navController = navController,
        // (nuevo) Por ahora, la app arranca directamente en Home.
        // (nuevo) En el Sprint 5 (Firebase Auth), cambiaremos esto para arrancar en LOGIN.
        startDestination = Routes.HOME
    ) {

        // (nuevo) Destino: Pantalla Home (lista de campañas).
        // (nuevo) La ruta está definida en Routes.HOME ("home").
        composable(Routes.HOME) {
            // (nuevo) Renderizamos la pantalla Home pasando el ViewModel.
            HomeScreen(
                viewModel = campaignViewModel,
                // (nuevo) Lambda que se ejecuta cuando el usuario toca una campaña.
                // (nuevo) Por ahora no navega a ningún lado (la implementaremos en el Paso 5).
                // (nuevo) En el Paso 5, esta lambda llamará a navController.navigate(Routes.campaignDetail(id))
                onCampaignClick = { campaign ->
                    // (nuevo) TODO: Implementar navegación al detalle en el Paso 5.
                    // (nuevo) Por ahora solo imprimimos en el log para verificar que el clic funciona.
                    println("Campaña seleccionada: ${campaign.titulo}")
                }
            )
        }

        // (nuevo) En futuros pasos agregaremos más destinos aquí:
        // composable(Routes.LOGIN) { LoginScreen() }
        // composable(Routes.CAMPAIGN_DETAIL) { CampaignDetailScreen() }
        // composable(Routes.REGISTER_TOY) { RegisterToyScreen() }
        // composable(Routes.TRACEABILITY) { TraceabilityScreen() }
        // composable(Routes.GALLERY) { GalleryScreen() }
        // composable(Routes.PROFILE) { ProfileScreen() }
        // composable(Routes.ADMIN_PANEL) { AdminPanelScreen() }
    }
}