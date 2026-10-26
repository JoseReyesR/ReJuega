// (modificado) Archivo: AppNavigation.kt
// (modificado) Descripción: NavHost principal de la app.
// (modificado) Cambio: Se conectan Home, Detalle de campaña y Registro de juguete.
package pe.rejuega.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pe.rejuega.app.di.AppContainer
import pe.rejuega.app.ui.screens.CampaignDetailScreen
import pe.rejuega.app.ui.screens.HomeScreen
import pe.rejuega.app.ui.screens.RegisterToyScreen
import pe.rejuega.app.ui.viewmodel.CampaignDetailViewModel
import pe.rejuega.app.ui.viewmodel.CampaignDetailViewModelFactory
import pe.rejuega.app.ui.viewmodel.CampaignViewModel
import pe.rejuega.app.ui.viewmodel.CampaignViewModelFactory
import pe.rejuega.app.ui.viewmodel.JugueteViewModel
import pe.rejuega.app.ui.viewmodel.JugueteViewModelFactory



// (modificado) Composable principal de navegación.

// (modificado) Archivo: AppNavigation.kt - imports
// (nuevo) Pantalla y ViewModel de trazabilidad.
import pe.rejuega.app.ui.screens.TraceabilityScreen
import pe.rejuega.app.ui.viewmodel.TraceabilityViewModel
import pe.rejuega.app.ui.viewmodel.TraceabilityViewModelFactory
@Composable
fun AppNavigation(
    navController: NavHostController,
    appContainer: AppContainer
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {

        // (modificado) Pantalla Home.
        composable(Routes.HOME) {
            val campaignViewModel: CampaignViewModel = viewModel(
                factory = CampaignViewModelFactory(appContainer.campaignRepository)
            )

            // (modificado) Archivo: AppNavigation.kt - composable(Routes.HOME)
            HomeScreen(
                viewModel = campaignViewModel,
                onCampaignClick = { campaign ->
                    navController.navigate(Routes.campaignDetail(campaign.id))
                },
                // (nuevo) Conecta el ícono de historial con la nueva pantalla.
                onTraceabilityClick = {
                    navController.navigate(Routes.TRACEABILITY)
                }
            )
        }

        // (modificado) Pantalla Detalle de campaña.
        composable(
            route = Routes.CAMPAIGN_DETAIL,
            arguments = listOf(
                navArgument("campaignId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->

            // (modificado) Recuperamos el ID de campaña recibido como argumento.
            val campaignId = backStackEntry.arguments?.getLong("campaignId") ?: 0L

            val detailViewModel: CampaignDetailViewModel = viewModel(
                factory = CampaignDetailViewModelFactory(
                    appContainer.campaignRepository,
                    campaignId
                )
            )

            CampaignDetailScreen(
                viewModel = detailViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onDonateClick = {
                    navController.navigate(Routes.registerToy(campaignId))
                }
            )
        }

        // (nuevo) Pantalla Registro de juguete.
        composable(
            route = Routes.REGISTER_TOY,
            arguments = listOf(
                navArgument("campaignId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->

            // (nuevo) Recuperamos el ID de campaña para asociar el juguete.
            val campaignId = backStackEntry.arguments?.getLong("campaignId") ?: 0L

            val jugueteViewModel: JugueteViewModel = viewModel(
                factory = JugueteViewModelFactory(
                    appContainer.jugueteRepository,
                    campaignId
                )
            )

            RegisterToyScreen(
                viewModel = jugueteViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onSaved = {
                    // (nuevo) Después de guardar, regresamos a la pantalla anterior.
                    navController.popBackStack()
                }
            )
        }
        // (nuevo) Destino: trazabilidad de donaciones (HU-06).
        composable(Routes.TRACEABILITY) {

            // (nuevo) Creamos el ViewModel inyectando el repositorio de juguetes.
            val traceabilityViewModel: TraceabilityViewModel = viewModel(
                factory = TraceabilityViewModelFactory(appContainer.jugueteRepository)
            )

            // (nuevo) Renderizamos la pantalla.
            TraceabilityScreen(
                viewModel = traceabilityViewModel,
                onBackClick = {
                    // (nuevo) Regresamos al Home sin duplicar la pantalla en la pila.
                    navController.popBackStack()
                }
            )
        }
    }
}