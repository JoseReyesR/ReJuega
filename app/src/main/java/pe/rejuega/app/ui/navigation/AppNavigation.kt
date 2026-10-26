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

            HomeScreen(
                viewModel = campaignViewModel,
                onCampaignClick = { campaign ->
                    // (modificado) Ahora sí navegamos al detalle con el ID de la campaña.
                    navController.navigate(Routes.campaignDetail(campaign.id))
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
    }
}