// (nuevo) Archivo: HomeScreen.kt
// (nuevo) Descripción: Pantalla principal (Home) que muestra la lista de campañas activas.
// (nuevo) Corresponde a la Historia de Usuario HU-02: "Explorar campañas con caché offline".
// (nuevo) Usa Material Design 3 y muestra los estados Loading, Success, Error y Empty.
package pe.rejuega.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
// (nuevo) Ícono de hoja para impacto ambiental.
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pe.rejuega.app.data.local.Campaign
import androidx.activity.compose.BackHandler
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import pe.rejuega.app.ui.components.MenuLateralReJuega
import pe.rejuega.app.viewmodel.CampaignViewModel
import pe.rejuega.app.utils.UiState

import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.foundation.layout.width

// (nuevo) Ícono de administración.
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Button


// (nuevo) Anotación requerida para usar TopAppBar con Material 3
@OptIn(ExperimentalMaterial3Api::class)
// (modificado) Archivo: HomeScreen.kt - firma de HomeScreen
// (nuevo) Se agrega onLogoutClick para cerrar sesión.
@Composable
fun HomeScreen(
    viewModel: CampaignViewModel,
    //  Indica si el usuario actual tiene rol Administrador.
    esAdministrador: Boolean = false,
    //  Nombre del usuario actual (para mostrar en la UI si se desea).
    nombreUsuario: String = "",
    // (nuevo) Indica si hay sincronización activa.
    estaSincronizado: Boolean = true,
    // (nuevo) Acción al hacer clic en una campaña.
    onCampaignClick: (Campaign) -> Unit,
    // (nuevo) Acción al hacer clic en el botón de Trazabilidad.
    onTraceabilityClick: () -> Unit = {},
    // (nuevo) Acción al hacer clic en el botón de Cerrar Sesión.
    onLogoutClick: () -> Unit = {},
    // (nuevo) Acción para abrir la pantalla de impacto ambiental.
    onImpactoClick: () -> Unit = {},
    // (nuevo) Acción para abrir la pantalla de Perfil.
    onPerfilClick: () -> Unit = {},
    // (nuevo) Acción para abrir el Panel Admin (solo se llama si es admin).
    onPanelAdminClick: () -> Unit = {},
    onCampaignAdminClick: () -> Unit = {},
    onDonationAdminClick: () -> Unit = {},
    onPuntosRecojoAdminClick: () -> Unit = {},

    notificacionesNoLeidas: Int = 0,
    onNotificationsClick: () -> Unit = {},

    // (nuevo) Filtro de estado de campañas.
    filtroEstado: String = "Activas",
    onFiltroChange: (String) -> Unit = {},
    // (nuevo) Acción para ver galería de evidencias.
    onVerEvidenciasClick: (Campaign) -> Unit = {},
    // (nuevo) Acción para ver mapa de puntos de recojo.
    onMapaClick: () -> Unit = {},
    // (nuevo) Acción para ver pruebas del servidor.
    onServidorClick: () -> Unit = {}
) {
    // (nuevo) Observamos el estado del ViewModel como State de Compose.
    // (nuevo) Cada vez que el ViewModel emita un nuevo estado, esta pantalla se recompondrá automáticamente.
    val uiState by viewModel.uiState.collectAsState()

    // (nuevo) Estado del menú lateral y scope para abrirlo/cerrarlo.
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // (nuevo) Si el menú está abierto, el botón atrás del teléfono lo cierra (no sale de la app).
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    // (nuevo) El menú lateral envuelve todo el Scaffold.
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MenuLateralReJuega(
                nombreUsuario = nombreUsuario,
                esAdmin = esAdministrador,
                notificacionesNoLeidas = notificacionesNoLeidas,
                onSeleccion = { destino ->
                    scope.launch { drawerState.close() }
                    when (destino) {
                        "panel" -> onPanelAdminClick()
                        "campaigns" -> onCampaignAdminClick()
                        "donations" -> onDonationAdminClick()
                        "puntos" -> onPuntosRecojoAdminClick()
                        "servidor" -> onServidorClick()
                        "trazabilidad" -> onTraceabilityClick()
                        "mapa" -> onMapaClick()
                        "impacto" -> onImpactoClick()
                        "notificaciones" -> onNotificationsClick()
                        "perfil" -> onPerfilClick()
                        "logout" -> onLogoutClick()
                    }
                }
            )
        }
    ) {
        // (nuevo) Scaffold es el esqueleto de Material 3: Proporciona TopAppBar, contenido, FAB, etc.
        Scaffold(
            // (nuevo) Barra superior (TopAppBar) con el menú lateral.
            topBar = {
                TopAppBar(
                    title = { Text("ReJuega", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Abrir menú",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    // (nuevo) Cierre de sesión rápido en la esquina superior derecha.
                    actions = {
                        IconButton(onClick = onLogoutClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Cerrar sesión",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // (nuevo) Aviso de modo offline cuando no hay sincronización.
            if (!estaSincronizado) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sin conexión. Mostrando campañas guardadas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // (nuevo) Chips de filtro de estado.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Activas", "Cerradas", "Todas").forEach { opcion ->
                    FilterChip(
                        selected = filtroEstado == opcion,
                        onClick = { onFiltroChange(opcion) },
                        label = { Text(opcion) }
                    )
                }
            }

            // (nuevo) Box para contener la lista o estados de carga.
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
            // (nuevo) Dependiendo del estado del ViewModel, mostramos diferente contenido.
            // (nuevo) Esto es el patrón "when" de Kotlin aplicado a la UI.
            when (val state = uiState) {

                // (nuevo) Estado LOADING: Mostramos un spinner de carga.
                is UiState.Loading -> {
                    // (nuevo) CircularProgressIndicator es el spinner circular de Material 3.
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // (nuevo) Estado SUCCESS: Mostramos la lista de campañas.
                is UiState.Success -> {
                    // (nuevo) Obtenemos la lista de campañas del estado.
                    val campaigns = state.data

                    // (nuevo) LazyColumn es el equivalente a RecyclerView pero en Compose.
                    // (nuevo) Solo renderiza los elementos visibles en pantalla (optimización de rendimiento).
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        // (nuevo) Padding interno para que las tarjetas no estén pegadas a los bordes.
                        contentPadding = PaddingValues(16.dp),
                        // (nuevo) Espacio vertical entre cada tarjeta.
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // (nuevo) Por cada campaña en la lista, renderizamos una tarjeta.
                        items(campaigns) { campaign ->
                            // (nuevo) Tarjeta individual de campaña.
                            CampaignCard(
                                campaign = campaign,
                                onClick = { onCampaignClick(campaign) },
                                onVerEvidencias = { onVerEvidenciasClick(campaign) }
                            )
                        }
                    }
                }

                // (nuevo) Estado ERROR: Mostramos un mensaje de error.
                is UiState.Error -> {
                    // (nuevo) Columna centrada para mostrar el mensaje de error.
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // (nuevo) Ícono de información para indicar que algo salió mal.
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Error",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // (nuevo) Mensaje de error en texto.
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // (nuevo) Estado EMPTY: Mostramos un mensaje de que no hay campañas.
                is UiState.Empty -> {
                    // (nuevo) Columna centrada para mostrar el mensaje vacío.
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // (nuevo) Ícono de calendario para indicar que no hay eventos/campañas.
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Sin campañas",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // (nuevo) Mensaje informativo.
                        Text(
                            text = "No hay campañas activas por el momento.\n¡Vuelve pronto!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
}
}

// (nuevo) Composable que representa una tarjeta individual de campaña.
// (nuevo) Se extrae como función separada para mantener el código limpio y reutilizable.
@Composable
fun CampaignCard(
    campaign: Campaign,
    onClick: () -> Unit,
    onVerEvidencias: (() -> Unit)? = null
) {
    // (nuevo) Card es el contenedor de Material 3 con elevación y esquinas redondeadas.
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        // (nuevo) Colores de la tarjeta: Fondo blanco con elevación sutil.
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        // (nuevo) Elevación (sombra) de la tarjeta.
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // (nuevo) Acción al hacer clic en la tarjeta.
        onClick = onClick
    ) {
        // (nuevo) Columna que contiene la imagen y los textos de la campaña.
        Column {
            // (nuevo) Imagen de la campaña cargada desde la URL con Coil.
            // (nuevo) AsyncImage carga la imagen de forma asíncrona (no bloquea la UI).
            AsyncImage(
                model = campaign.imagenUrl, // (nuevo) URL de la imagen.
                contentDescription = "Imagen de la campaña ${campaign.titulo}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp), // (nuevo) Altura fija para la imagen.
                contentScale = ContentScale.Crop // (nuevo) Recorta la imagen para llenar el espacio.
            )

            // (nuevo) Contenido de texto de la tarjeta.
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // (nuevo) Título de la campaña.
                Text(
                    text = campaign.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // (nuevo) Descripción de la campaña (máximo 2 líneas para no ocupar mucho espacio).
                Text(
                    text = campaign.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // (nuevo) Fila con las fechas de la campaña.
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // (nuevo) Ícono de calendario.
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Fecha",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    // (nuevo) Texto con el rango de fechas.
                    Text(
                        text = "${campaign.fechaInicio} → ${campaign.fechaFin}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // (nuevo) Chip de estado de la campaña (Activa, Cerrada, etc.)
                // (nuevo) Usamos un Row con un pequeño círculo de color como indicador.
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // (nuevo) Indicador circular de estado.
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .padding(0.dp)
                            // (nuevo) El color depende del estado: Verde si está Activa, Gris si está Cerrada.
                            .fillMaxSize()
                        // (nuevo) Aquí se podría usar .background() para dar color, pero lo simplificamos.
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    // (nuevo) Texto con el estado de la campaña.
                    Text(
                        text = campaign.estado,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        // (nuevo) Color del estado: Verde si está Activa, otro color si no.
                        color = if (campaign.estado == "Activa")
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }

                // (nuevo) Botón de evidencias solo si está cerrada y hay acción.
                if (campaign.estado == "Cerrada" && onVerEvidencias != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onVerEvidencias() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver galería de evidencias")
                    }
                }
            }
        }
    }
}