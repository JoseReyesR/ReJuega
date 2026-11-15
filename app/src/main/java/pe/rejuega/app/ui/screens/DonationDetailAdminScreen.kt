// (nuevo) Pantalla de detalle con acciones para avanzar estado o rechazar.
package pe.rejuega.app.ui.screens

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pe.rejuega.app.viewmodel.DonationAdminUiState
import pe.rejuega.app.viewmodel.DonationAdminViewModel
import pe.rejuega.app.utils.EstadosTrazabilidad
// (nuevo) Imports para el Snackbar de errores.
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationDetailAdminScreen(
    viewModel: DonationAdminViewModel,
    donacionId: String,
    onBackClick: () -> Unit,
    onSaved: () -> Unit
) {
    val donations by viewModel.donations.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    // (nuevo) Estado del Snackbar para mostrar errores visibles.
    val snackbarHostState = remember { SnackbarHostState() }

    // (modificado) Se busca por ID de documento (antes comparaba uid y nombre).
    val donacion = donations.find { it.id == donacionId }


    // (nuevo) Como usamos navegación con ID, buscamos por todos los campos.
    // (nuevo) En una app real usaríamos el docId de Firestore.
    var mostrarDialogoRechazo by remember { mutableStateOf(false) }
    var motivoRechazo by rememberSaveable { mutableStateOf("") }

    // (modificado) Ahora se muestran los errores en Snackbar, no se tragan en silencio.
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is DonationAdminUiState.Success -> {
                viewModel.limpiarEstado()
                onSaved()
            }
            is DonationAdminUiState.Error -> {
                snackbarHostState.showSnackbar(state.mensaje)
                viewModel.limpiarEstado()
            }
            else -> Unit
        }
    }

    Scaffold(
        // (nuevo) Host del Snackbar para mensajes de éxito/error.
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Donación", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (donacion == null) {
            Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Text("No se encontró la donación.")
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                AsyncImage(
                    model = donacion.fotoUrl,
                    contentDescription = donacion.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(16.dp))

                Text(donacion.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "Estado actual: ${donacion.estadoTrazabilidad}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text("Descripción", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(donacion.descripcion, style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(12.dp))
                Text("Estado físico: ${donacion.estado}", style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(20.dp))

                // (nuevo) Acciones solo si no está entregada ni rechazada.
                val puedeAvanzar = donacion.estadoTrazabilidad != "Entregado"
                        && donacion.estadoTrazabilidad != "Rechazado"

                if (puedeAvanzar) {
                    val siguiente = EstadosTrazabilidad.siguienteDe(donacion.estadoTrazabilidad)
                    if (siguiente != null) {
                        Button(
                            // (modificado) Se pasa la donación completa para que el ViewModel cree la notificación.
                            onClick = { viewModel.avanzarEstado(donacion.id, donacion.estadoTrazabilidad, donacion) },
                            enabled = uiState !is DonationAdminUiState.Loading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (uiState is DonationAdminUiState.Loading) {
                                CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(Modifier.size(8.dp))
                                Text("Avanzar a: $siguiente")
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { mostrarDialogoRechazo = true },
                        enabled = uiState !is DonationAdminUiState.Loading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Rechazar donación")
                    }
                } else {
                    Text(
                        text = "Esta donación ya está en estado final: ${donacion.estadoTrazabilidad}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // (nuevo) Diálogo de rechazo con motivo obligatorio.
    if (mostrarDialogoRechazo && donacion != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoRechazo = false },
            title = { Text("Rechazar donación") },
            text = {
                Column {
                    Text("Indica el motivo del rechazo:")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = motivoRechazo,
                        onValueChange = { motivoRechazo = it },
                        label = { Text("Motivo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (motivoRechazo.isNotBlank()) {
                            // (modificado) Se usa el ID del documento Firestore.
                            // (modificado) Se pasa la donación completa para que el ViewModel cree la notificación.
                            viewModel.rechazar(donacion.id, motivoRechazo, donacion)
                            mostrarDialogoRechazo = false
                            motivoRechazo = ""
                        }
                    },
                    enabled = motivoRechazo.isNotBlank()
                ) { Text("Rechazar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoRechazo = false
                    motivoRechazo = ""
                }) { Text("Cancelar") }
            }
        )
    }
}