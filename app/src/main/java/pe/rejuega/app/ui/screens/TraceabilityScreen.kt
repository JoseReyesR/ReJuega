// (modificado) Trazabilidad real del donante desde Firestore (HU-07).
// (modificado) Se elimina el botón simulado de Admin: ahora solo lectura.
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pe.rejuega.app.data.remote.Donacion
import pe.rejuega.app.ui.components.LineaDeTiempoDonacion
import pe.rejuega.app.viewmodel.TraceabilityViewModel
import pe.rejuega.app.utils.formatarFechaHora

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraceabilityScreen(
    viewModel: TraceabilityViewModel,
    onBackClick: () -> Unit
) {
    val donaciones by viewModel.donaciones.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mis donaciones", fontWeight = FontWeight.Bold) },
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
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
                donaciones.isEmpty() -> Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Aún no has registrado donaciones.")
                    Spacer(Modifier.height(8.dp))
                    Text("Explora una campaña y haz tu primera donación.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(donaciones) { donacion ->
                        DonacionTraceabilityCard(donacion)
                    }
                }
            }
        }
    }
}

@Composable
private fun DonacionTraceabilityCard(donacion: Donacion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = donacion.fotoUrl,
                    contentDescription = donacion.nombre,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(donacion.nombre, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    // (nuevo) Campaña a la que pertenece (campo desnormalizado).
                    Text(donacion.campaignTitulo.ifBlank { "Campaña no indicada" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text("Condición: ${donacion.estado}",
                        style = MaterialTheme.typography.bodySmall)
                    Text("Registrada: ${formatarFechaHora(donacion.fechaRegistro)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Estado actual: ${donacion.estadoTrazabilidad.replace("En Revision", "En revisión")}",
                style = MaterialTheme.typography.labelLarge,
                color = if (donacion.estadoTrazabilidad == "Rechazado")
                    MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(12.dp))

            // (nuevo) Línea de tiempo con fechas reales del historial.
            LineaDeTiempoDonacion(
                historial = donacion.historialEstados,
                estadoActual = donacion.estadoTrazabilidad
            )

            // (nuevo) Motivo de rechazo visible para el donante.
            if (donacion.estadoTrazabilidad == "Rechazado" && donacion.motivoRechazo != null) {
                Spacer(Modifier.height(8.dp))
                Text("Motivo: ${donacion.motivoRechazo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error)
            }
        }
    }
}