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
// (modificado) Archivo: AppNavigation.kt - imports
import pe.rejuega.app.ui.screens.PerfilScreen
import pe.rejuega.app.ui.viewmodel.PerfilViewModel
import pe.rejuega.app.ui.viewmodel.PerfilViewModelFactory

// (nuevo) Imports necesarios para el nuevo factory del JugueteViewModel.
import android.app.Application
import androidx.compose.ui.platform.LocalContext

// (nuevo) Imports para el SessionViewModel y el Panel Admin.
import androidx.compose.runtime.LaunchedEffect
import pe.rejuega.app.ui.screens.PanelAdminScreen
import pe.rejuega.app.ui.viewmodel.SessionViewModel
import pe.rejuega.app.ui.viewmodel.SessionViewModelFactory
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue


import pe.rejuega.app.ui.screens.CampaignAdminScreen
import pe.rejuega.app.ui.screens.CampaignGalleryScreen
import pe.rejuega.app.ui.viewmodel.CampaignGalleryViewModel
import pe.rejuega.app.ui.viewmodel.CampaignGalleryViewModelFactory
import pe.rejuega.app.ui.screens.CampaignFormScreen
import pe.rejuega.app.ui.viewmodel.CampaignAdminViewModel
import pe.rejuega.app.ui.viewmodel.CampaignAdminViewModelFactory

import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

import pe.rejuega.app.ui.screens.DonationAdminScreen
import pe.rejuega.app.ui.screens.DonationDetailAdminScreen
import pe.rejuega.app.ui.viewmodel.DonationAdminViewModel
import pe.rejuega.app.ui.viewmodel.DonationAdminViewModelFactory


import androidx.compose.runtime.getValue
import pe.rejuega.app.ui.screens.NotificationScreen
import pe.rejuega.app.ui.viewmodel.NotificationViewModel
import pe.rejuega.app.ui.viewmodel.NotificationViewModelFactory


import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

// (modificado) Composable principal de navegación.

@Composable
fun AppNavigation(
    navController: NavHostController,
    appContainer: AppContainer
) {

    // (nuevo) Creamos el SessionViewModel a nivel de navegación.
    // (nuevo) Este ViewModel mantiene el usuario actual y su rol disponibles para todas las pantallas que lo necesiten.

    val sessionViewModel: SessionViewModel = viewModel(
        factory = SessionViewModelFactory(appContainer.authRepository)
    )
    // (nuevo) Observamos el estado de la sesión para saber si hay usuario.
    val sessionState by sessionViewModel.sessionState.collectAsState()

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
            // (nuevo) Cargamos el usuario actual cuando se muestra el Home.
            // (nuevo) Esto asegura que el rol esté disponible después del login.
            LaunchedEffect(Unit) {
                sessionViewModel.cargarUsuarioActual()
            }

            val campaignViewModel: CampaignViewModel = viewModel(
                factory = CampaignViewModelFactory(
                    appContainer.campaignSyncRepository
                )
            )
            // (nuevo) Refresca las campañas cada vez que el Home vuelve a estar visible.
            // (nuevo) Resuelve el problema de que el Home no se actualiza al regresar
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        campaignViewModel.refrescar()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }
            // (nuevo) Observamos el estado de la sesión para obtener el rol.
            val sessionState by sessionViewModel.sessionState.collectAsState()
            // (nuevo) Observamos el estado de sincronización para el aviso offline.
            val estaSincronizado by campaignViewModel.estaSincronizado.collectAsState()
            // (nuevo) Filtro de campañas.
            val filtro by campaignViewModel.filtro.collectAsState()

            // (nuevo) Observa el conteo de no leídas.
            val uidUsuario = sessionState.usuario?.uid ?: ""
            val notificationRepository = appContainer.notificationFirestoreRepository
            var noLeidas by remember { mutableIntStateOf(0) }
            LaunchedEffect(uidUsuario) {
                if (uidUsuario.isNotBlank()) {
                    try {
                        notificationRepository.contarNoLeidas(uidUsuario).collect { noLeidas = it }
                    } catch (e: Exception) {
                        noLeidas = 0
                    }
                } else {
                    noLeidas = 0
                }
            }
            HomeScreen(
                viewModel = campaignViewModel,
                // (nuevo) Pasamos si el usuario es admin para mostrar/ocultar el Panel Admin.
                esAdministrador = sessionViewModel.esAdministrador(),
                nombreUsuario = sessionState.usuario?.nombre ?: "",
                // (nuevo) Pasamos el estado de sincronización.
                estaSincronizado = estaSincronizado,
                filtroEstado = filtro,
                onFiltroChange = { campaignViewModel.cambiarFiltro(it) },
                onVerEvidenciasClick = { campaign ->
                    navController.navigate(Routes.campaignGallery(campaign.firestoreId, campaign.titulo))
                },
                onCampaignClick = { campaign ->
                    navController.navigate(Routes.campaignDetail(campaign.id))
                },
                onTraceabilityClick = {
                    navController.navigate(Routes.TRACEABILITY)
                },
                // (nuevo) Cierre de sesión.
                onLogoutClick = {
                    appContainer.authRepository.cerrarSesion()
                    // (nuevo) Limpiamos la sesión en el SessionViewModel.
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) {
                            inclusive = true
                        }
                    }
                },
                // (nuevo) Conecta el ícono de impacto con la nueva pantalla.
                onImpactoClick = {
                    navController.navigate(Routes.IMPACTO_AMBIENTAL)
                },

                // (nuevo) Conecta el ícono de perfil con la nueva pantalla.
                onPerfilClick = {
                    navController.navigate(Routes.PERFIL)
                },
                // (nuevo) Conecta el ícono de panel admin con la nueva pantalla.
                onPanelAdminClick = {
                    navController.navigate(Routes.PANEL_ADMIN)
                },
                notificacionesNoLeidas = noLeidas,
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) }

            )


        }

        // (nuevo) Destino: Panel de Administración (solo para rol Administrador).
// (nuevo) Implementa RN-04: se verifica el rol antes de mostrar la pantalla.
        composable(Routes.PANEL_ADMIN) {

            // (nuevo) PROTECCIÓN POR ROL: verificamos que el usuario sea Administrador.
            // (nuevo) Si no es admin, regresamos inmediatamente sin mostrar la pantalla.
            // (nuevo) Esto es una segunda capa de protección (la primera es ocultar el botón en el Home).
            if (!sessionViewModel.esAdministrador()) {
                // (nuevo) El usuario no es admin, lo regresamos sin renderizar el panel.
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            } else {
                // (nuevo) El usuario es admin, mostramos el panel.
                val nombreAdmin = sessionViewModel.sessionState.collectAsState().value.usuario?.nombre ?: "Admin"

                PanelAdminScreen(
                    nombreAdmin = nombreAdmin,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    // (nuevo) Navega a gestión de campañas.
                    onGestionarCampaigns = {
                        navController.navigate(Routes.CAMPAIGN_ADMIN)
                    },
                    // (nuevo) Navegación a revisar donaciones.
                    onRevisarDonaciones = { navController.navigate(Routes.DONATION_ADMIN) },
                    onGaleriaEvidencias = { navController.navigate(Routes.CAMPAIGN_ADMIN) }

                )
            }
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
                // (modificado) Navega con el ID y el título de la campaña.
                onDonateClick = { titulo ->
                    navController.navigate(Routes.registerToy(campaignId, titulo))
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
            // (nuevo) Título de la campaña (viene codificado y Navigation lo decodifica).
            val campaignTitulo = backStackEntry.arguments?.getString("campaignTitulo") ?: ""
            // (nuevo) Obtenemos el contexto de la aplicación para el AndroidViewModel.
            val context = LocalContext.current
            val application = context.applicationContext as Application
            // (nuevo) Obtenemos el UID del usuario autenticado en Firebase.
            // (nuevo) Este UID se guardará junto con la donación en Firestore.
            val uidUsuario = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            val jugueteViewModel: JugueteViewModel = viewModel(
                factory = JugueteViewModelFactory(
                    application = application,
                    donationRepository = appContainer.donationRepository,
                    campaignId = campaignId,
                    campaignTitulo = campaignTitulo   // (nuevo)
                )
            )

            RegisterToyScreen(
                viewModel = jugueteViewModel,
                uidUsuario = uidUsuario,
                onBackClick = {
                    navController.popBackStack()
                },
                onSaved = {
                    navController.popBackStack()
                }
            )
        }

        // (modificado) Trazabilidad con uid real y repository de Firestore.
        composable(Routes.TRACEABILITY) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val traceabilityViewModel: TraceabilityViewModel = viewModel(
                factory = TraceabilityViewModelFactory(
                    appContainer.donationFirestoreRepository, uid
                )
            )
            TraceabilityScreen(
                viewModel = traceabilityViewModel,
                onBackClick = { navController.popBackStack() }
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



        // (nuevo) Destino: Pantalla de Perfil (HU-12).
        composable(Routes.PERFIL) {

            // (nuevo) Obtenemos el UID del usuario actual de Firebase para pasarlo al ViewModel.
            val uidUsuario = FirebaseAuth.getInstance().currentUser?.uid ?: "usuario_anonimo"

            // (nuevo) Creamos el ViewModel inyectando los repositorios y el UID.
            val perfilViewModel: PerfilViewModel = viewModel(
                factory = PerfilViewModelFactory(
                    jugueteRepository = appContainer.jugueteRepository,
                    materialEcoRepository = pe.rejuega.app.data.repository.MaterialEcoRepository(),
                    uidUsuario = uidUsuario
                )
            )

            PerfilScreen(
                viewModel = perfilViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // (nuevo) Destino: gestión de campañas (Admin).
        composable(Routes.CAMPAIGN_ADMIN) {
            val uidAdmin = sessionViewModel.sessionState.collectAsState().value.usuario?.uid ?: ""
            val campaignAdminViewModel: CampaignAdminViewModel = viewModel(
                factory = CampaignAdminViewModelFactory(
                    appContainer.campaignFirestoreRepository, uidAdmin
                )
            )
            CampaignAdminScreen(
                viewModel = campaignAdminViewModel,
                onBackClick = { navController.popBackStack() },
                onCrearClick = { navController.navigate(Routes.campaignForm("new")) },
                onEditarClick = { id -> navController.navigate(Routes.campaignForm(id)) },
                onVerGaleriaClick = { id, titulo ->
                    navController.navigate(Routes.campaignGallery(id, titulo))
                }
            )
        }

        // (nuevo) Destino: formulario de campaña.
        composable(Routes.CAMPAIGN_FORM) { backStackEntry ->
            val campaignId = backStackEntry.arguments?.getString("campaignId") ?: "new"
            val uidAdmin = sessionViewModel.sessionState.collectAsState().value.usuario?.uid ?: ""
            val campaignAdminViewModel: CampaignAdminViewModel = viewModel(
                factory = CampaignAdminViewModelFactory(
                    appContainer.campaignFirestoreRepository, uidAdmin
                )
            )
            CampaignFormScreen(
                viewModel = campaignAdminViewModel,
                campaignId = campaignId,
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        // (nuevo) Destino: lista de donaciones pendientes.
        composable(Routes.DONATION_ADMIN) {
            val uidAdmin = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val donationAdminViewModel: DonationAdminViewModel = viewModel(
                factory = DonationAdminViewModelFactory(
                    appContainer.donationFirestoreRepository,
                    appContainer.notificationFirestoreRepository,
                    uidAdmin
                )
            )
            DonationAdminScreen(
                viewModel = donationAdminViewModel,
                onBackClick = { navController.popBackStack() },
                onDonacionClick = { donacion ->
                    // (modificado) Se navega con el ID del documento Firestore, no con el uid del donante.
                    navController.navigate(Routes.donationDetailAdmin(donacion.id))
                }
            )
        }

// (nuevo) Destino: detalle de donación.
        composable(Routes.DONATION_DETAIL_ADMIN) { backStackEntry ->
            val donacionUid = backStackEntry.arguments?.getString("donacionUid") ?: ""
            val uidAdmin = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val donationAdminViewModel: DonationAdminViewModel = viewModel(
                factory = DonationAdminViewModelFactory(
                    appContainer.donationFirestoreRepository,
                    appContainer.notificationFirestoreRepository,
                    uidAdmin
                )
            )
            DonationDetailAdminScreen(
                viewModel = donationAdminViewModel,
                donacionId = donacionUid,
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Routes.NOTIFICATIONS) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val vm: NotificationViewModel = viewModel(
                factory = NotificationViewModelFactory(
                    appContainer.notificationFirestoreRepository, uid
                )
            )
            NotificationScreen(viewModel = vm, onBackClick = { navController.popBackStack() })
        }

        // (nuevo) Destino: Galería de evidencias de una campaña (HU-09).
        composable(Routes.CAMPAIGN_GALLERY) { backStackEntry ->
            val campaignId = backStackEntry.arguments?.getString("campaignId") ?: ""
            val campaignTitulo = backStackEntry.arguments?.getString("campaignTitulo") ?: ""
            val esAdmin = sessionViewModel.esAdministrador()
            val uidAdmin = if (esAdmin) FirebaseAuth.getInstance().currentUser?.uid else null

            val galleryViewModel: CampaignGalleryViewModel = viewModel(
                factory = CampaignGalleryViewModelFactory(
                    repository = appContainer.campaignGalleryRepository,
                    campaignId = campaignId,
                    campaignTitulo = campaignTitulo,
                    uidAdmin = uidAdmin
                )
            )

            CampaignGalleryScreen(
                viewModel = galleryViewModel,
                esAdmin = esAdmin,
                onBackClick = { navController.popBackStack() }
            )
        }


    }
}