// (modificado) Archivo: AppNavigation.kt
// (modificado) Descripción: NavHost principal de ReJuega.
// (modificado) Cambio: Se agrega Login, sesión inicial y cierre de sesión.
package pe.rejuega.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import pe.rejuega.app.di.AppContainer
import pe.rejuega.app.ui.screens.CampaignDetailScreen
import pe.rejuega.app.ui.screens.HomeScreen
import pe.rejuega.app.ui.screens.LoginScreen
import pe.rejuega.app.ui.screens.RegisterToyScreen
import pe.rejuega.app.ui.screens.TraceabilityScreen
import pe.rejuega.app.ui.viewmodel.AuthViewModel
import pe.rejuega.app.ui.viewmodel.AuthViewModelFactory
import pe.rejuega.app.ui.viewmodel.CampaignDetailViewModel
import pe.rejuega.app.ui.viewmodel.CampaignDetailViewModelFactory
import pe.rejuega.app.ui.viewmodel.CampaignViewModel
import pe.rejuega.app.ui.viewmodel.CampaignViewModelFactory
import pe.rejuega.app.ui.viewmodel.JugueteViewModel
import pe.rejuega.app.ui.viewmodel.JugueteViewModelFactory
import pe.rejuega.app.ui.viewmodel.TraceabilityViewModel
import pe.rejuega.app.ui.viewmodel.TraceabilityViewModelFactory

// (nuevo) Pantalla y ViewModel de impacto ambiental.
import pe.rejuega.app.ui.screens.ImpactoAmbientalScreen
import pe.rejuega.app.ui.viewmodel.ImpactoAmbientalViewModel
import pe.rejuega.app.ui.viewmodel.ImpactoAmbientalViewModelFactory
import pe.rejuega.app.data.repository.MaterialEcoRepository


// (modificado) Composable principal de navegación.
@Composable
fun AppNavigation(
    navController: NavHostController,
    appContainer: AppContainer
) {

    // (nuevo) Verificamos si Firebase ya tiene una sesión activa.
    val existeSesion = remember {
        FirebaseAuth.getInstance().currentUser != null
    }

    // (nuevo) Definimos la pantalla inicial según el estado de sesión.
    val startDestination = if (existeSesion) {
        Routes.HOME
    } else {
        Routes.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // (nuevo) Destino: Login.
        composable(Routes.LOGIN) {

            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(appContainer.authRepository)
            )

            LoginScreen(
                viewModel = authViewModel,
                onAuthenticated = {
                    // (nuevo) Después de autenticarse, vamos al Home.
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // (modificado) Destino: Home.
        composable(Routes.HOME) {

            val campaignViewModel: CampaignViewModel = viewModel(
                factory = CampaignViewModelFactory(appContainer.campaignRepository)
            )

            HomeScreen(
                viewModel = campaignViewModel,
                onCampaignClick = { campaign ->
                    navController.navigate(Routes.campaignDetail(campaign.id))
                },
                onTraceabilityClick = {
                    navController.navigate(Routes.TRACEABILITY)
                },
                // (nuevo) Cierre de sesión.
                onLogoutClick = {
                    appContainer.authRepository.cerrarSesion()

                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) {
                            inclusive = true
                        }
                    }
                },
                // (nuevo) Conecta el ícono de impacto con la nueva pantalla.
                onImpactoClick = {
                    navController.navigate(Routes.IMPACTO_AMBIENTAL)
                }
            )
        }

        // (modificado) Destino: Detalle de campaña.
        composable(
            route = Routes.CAMPAIGN_DETAIL,
            arguments = listOf(
                navArgument("campaignId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->

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

        // (modificado) Destino: Registro de juguete.
        composable(
            route = Routes.REGISTER_TOY,
            arguments = listOf(
                navArgument("campaignId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->

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
                    navController.popBackStack()
                }
            )
        }

        // (modificado) Destino: Trazabilidad.
        composable(Routes.TRACEABILITY) {

            val traceabilityViewModel: TraceabilityViewModel = viewModel(
                factory = TraceabilityViewModelFactory(appContainer.jugueteRepository)
            )

            TraceabilityScreen(
                viewModel = traceabilityViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        // (nuevo) Destino: Pantalla de impacto ambiental (HU-11, ODS 12).
        composable(Routes.IMPACTO_AMBIENTAL) {

            // (nuevo) Creamos el repositorio de materiales ecológicos.
            val materialEcoRepository = MaterialEcoRepository()

            // (nuevo) Creamos el ViewModel inyectando el repositorio.
            val impactoViewModel: ImpactoAmbientalViewModel = viewModel(
                factory = ImpactoAmbientalViewModelFactory(materialEcoRepository)
            )

            // (nuevo) Renderizamos la pantalla.
            ImpactoAmbientalScreen(
                viewModel = impactoViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

    }
}