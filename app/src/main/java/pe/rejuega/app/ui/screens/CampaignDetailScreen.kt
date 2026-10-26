// (nuevo) Archivo: CampaignDetailScreen.kt
// (nuevo) Descripción: Pantalla de detalle de una campaña.
// (nuevo) Corresponde a HU-02: Explorar campañas con caché offline.
package pe.rejuega.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import pe.rejuega.app.ui.viewmodel.CampaignDetailViewModel
import pe.rejuega.app.utils.UiState

// (nuevo) Se requiere OptIn porque TopAppBar de Material 3 es experimental.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDetailScreen(
    viewModel: CampaignDetailViewModel,
    onBackClick: () -> Unit,
    onDonateClick: () -> Unit
) {
    // (nuevo) Observamos el estado del ViewModel.
    val uiState by viewModel.uiState.collectAsState()

    // (nuevo) Estructura base de la pantalla.
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle de campaña",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
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

        // (nuevo) Contenido principal.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // (nuevo) Mostramos diferente contenido según el estado.
            when (val state = uiState) {

                // (nuevo) Estado cargando.
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // (nuevo) Estado exitoso.
                is UiState.Success -> {
                    val campaign = state.data

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // (nuevo) Imagen principal de la campaña.
                        AsyncImage(
                            model = campaign.imagenUrl,
                            contentDescription = "Imagen de la campaña ${campaign.titulo}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentScale = ContentScale.Crop
                        )

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // (nuevo) Título de la campaña.
                            Text(
                                text = campaign.titulo,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // (nuevo) Estado de la campaña.
                            Text(
                                text = "Estado: ${campaign.estado}",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (campaign.estado == "Activa") {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // (nuevo) Fechas de inicio y fin.
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Fechas",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(
                                    text = "${campaign.fechaInicio} → ${campaign.fechaFin}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // (nuevo) Descripción completa.
                            Text(
                                text = "Descripción",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = campaign.descripcion,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // (nuevo) Botón para registrar un juguete en esta campaña.
                            Button(
                                onClick = onDonateClick,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = campaign.estado == "Activa"
                            ) {
                                Text(text = "Donar juguete")
                            }

                            // (nuevo) Si la campaña no está activa, se muestra un mensaje.
                            if (campaign.estado != "Activa") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Esta campaña ya no admite nuevas donaciones.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // (nuevo) Estado de error.
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Error",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // (nuevo) Estado vacío.
                is UiState.Empty -> {
                    Text(
                        text = "No hay información disponible.",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}