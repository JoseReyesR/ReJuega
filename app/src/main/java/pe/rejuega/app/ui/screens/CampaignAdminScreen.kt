// (modificado) Archivo: CampaignAdminScreen.kt
// (modificado) Cambio: Refresh automático al retroceder (ON_RESUME), borrado con confirmación
// (modificado) y fechas formateadas visibles.
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import pe.rejuega.app.data.remote.CampaignFirestore
import pe.rejuega.app.utils.isoAFechaVisible
import pe.rejuega.app.viewmodel.CampaignAdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignAdminScreen(
    viewModel: CampaignAdminViewModel,
    onBackClick: () -> Unit,
    onCrearClick: () -> Unit,
    onEditarClick: (String) -> Unit,
    onVerGaleriaClick: (id: String, titulo: String) -> Unit = { _, _ -> }
) {
    val campaigns by viewModel.campaigns.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var campaignAEliminar by remember { mutableStateOf<CampaignFirestore?>(null) }

    // (nuevo) REFRESH AUTOMÁTICO AL RETROCEDER.
    val lifecycleOwner = LocalLifecycleOwner.current
    var fueVisible by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && fueVisible) {
                viewModel.refrescarCampaigns()
            }
            if (event == Lifecycle.Event.ON_RESUME) fueVisible = true
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Campañas", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCrearClick) {
                Icon(Icons.Default.Add, contentDescription = "Crear campaña")
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refrescarCampaigns() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (campaigns.isEmpty() && !isRefreshing) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay campañas registradas.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(campaigns) { campaign ->
                        CampaignAdminCard(
                            campaign = campaign,
                            onEditarClick = { onEditarClick(campaign.id) },
                            onCerrarClick = { viewModel.cerrarCampaign(campaign.id) },
                            onVerGaleriaClick = { onVerGaleriaClick(campaign.id, campaign.titulo) },
                            onEliminar = { campaignAEliminar = campaign }
                        )
                    }
                }
            }
        }
    }

    // (nuevo) Confirmación fuerte antes de eliminar (borrado en cascada).
    campaignAEliminar?.let { campaign ->
        AlertDialog(
            onDismissRequest = { campaignAEliminar = null },
            title = { Text("Eliminar campaña") },
            text = {
                Column {
                    Text("¿Seguro que deseas eliminar '${campaign.titulo}'?", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Esta acción es irreversible y también eliminará todas las donaciones, eventos y evidencias asociadas.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarCampaign(campaign.id)
                    campaignAEliminar = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { campaignAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun CampaignAdminCard(
    campaign: CampaignFirestore,
    onEditarClick: () -> Unit,
    onCerrarClick: () -> Unit,
    onVerGaleriaClick: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(campaign.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(campaign.descripcion, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            Text("${isoAFechaVisible(campaign.fechaInicio)} → ${isoAFechaVisible(campaign.fechaFin)}", style = MaterialTheme.typography.bodySmall)
            Text(
                text = campaign.estado,
                style = MaterialTheme.typography.labelMedium,
                color = if (campaign.estado == "Activa") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
            Column {
                Row {
                    OutlinedButton(
                        onClick = onEditarClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, null, Modifier.width(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Editar", maxLines = 1)
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onVerGaleriaClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, Modifier.width(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Galería", maxLines = 1)
                    }
                }
                if (campaign.estado == "Activa") {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onCerrarClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Lock, null, Modifier.width(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Cerrar campaña", maxLines = 1)
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onEliminar,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, null, Modifier.width(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Eliminar campaña", maxLines = 1)
                }
            }
        }
    }
}
