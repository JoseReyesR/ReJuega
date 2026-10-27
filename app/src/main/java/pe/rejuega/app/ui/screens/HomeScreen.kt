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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
// (nuevo) Ícono de hoja para impacto ambiental.
import androidx.compose.material.icons.filled.Eco
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
import pe.rejuega.app.ui.viewmodel.CampaignViewModel
import pe.rejuega.app.utils.UiState

// (nuevo) Anotación requerida para usar TopAppBar con Material 3
@OptIn(ExperimentalMaterial3Api::class)
// (modificado) Archivo: HomeScreen.kt - firma de HomeScreen
// (nuevo) Se agrega onLogoutClick para cerrar sesión.
@Composable
fun HomeScreen(
    viewModel: CampaignViewModel,
    onCampaignClick: (Campaign) -> Unit,
    onTraceabilityClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    // (nuevo) Acción para abrir la pantalla de impacto ambiental.
    onImpactoClick: () -> Unit = {}

) {
    // (nuevo) Observamos el estado del ViewModel como State de Compose.
    // (nuevo) Cada vez que el ViewModel emita un nuevo estado, esta pantalla se recompondrá automáticamente.
    val uiState by viewModel.uiState.collectAsState()

    // (nuevo) Scaffold es el esqueleto de Material 3: Proporciona TopAppBar, contenido, FAB, etc.
    Scaffold(
        // (nuevo) Barra superior (TopAppBar) con el título de la app.
        topBar = {
            TopAppBar(
                // (nuevo) Título principal de la app.
                title = {
                    Column {
                        // (nuevo) Nombre de la app.
                        Text(
                            text = "ReJuega",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        // (nuevo) Subtítulo que indica qué está viendo el usuario.
                        Text(
                            text = "Campañas activas de donación",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },

                // (nuevo) Colores de la barra superior: Usamos el color primario (Verde Esperanza).
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                // (modificado) Archivo: HomeScreen.kt - actions del TopAppBar
                // (nuevo) Se agrega botón de cierre de sesión.
                actions = {
                    IconButton(onClick = onTraceabilityClick) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Ver trazabilidad de mis donaciones",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    // (nuevo) Botón para acceder a la pantalla de impacto ambiental.
                    IconButton(onClick = onImpactoClick) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = "Ver impacto ambiental",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

            )
        }
    ) { paddingValues ->
        // (nuevo) Box para centrar el contenido y aplicar el padding del Scaffold.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                                // (nuevo) Cuando el usuario toque la tarjeta, llamamos a la lambda de navegación.
                                onClick = { onCampaignClick(campaign) }
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

// (nuevo) Composable que representa una tarjeta individual de campaña.
// (nuevo) Se extrae como función separada para mantener el código limpio y reutilizable.
@Composable
fun CampaignCard(
    campaign: Campaign,
    onClick: () -> Unit
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
            }
        }
    }
}