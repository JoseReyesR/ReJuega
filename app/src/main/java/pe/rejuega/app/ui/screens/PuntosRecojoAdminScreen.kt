// (nuevo) Pantalla de gestión de puntos fijos y eventos de recojo.
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.rejuega.app.data.remote.PickupEventFirestore
import pe.rejuega.app.data.remote.PuntoEntregaFirestore
import pe.rejuega.app.viewmodel.PuntosRecojoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuntosRecojoAdminScreen(
    viewModel: PuntosRecojoViewModel,
    onBackClick: () -> Unit,
    onCrearPunto: () -> Unit,
    onCrearEvento: () -> Unit,
    onEditarPunto: (String) -> Unit,
    onEditarEvento: (String) -> Unit
) {
    val puntos by viewModel.puntos.collectAsState()
    val eventos by viewModel.eventos.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var tab by rememberSaveable { mutableStateOf("Puntos fijos") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensaje, uiState.error) {
        uiState.mensaje?.let { snackbarHostState.showSnackbar(it); viewModel.limpiarEstado() }
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.limpiarEstado() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Puntos de Recojo", fontWeight = FontWeight.Bold) },
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
            FloatingActionButton(onClick = { if (tab == "Puntos fijos") onCrearPunto() else onCrearEvento() }) {
                Icon(Icons.Default.Add, contentDescription = "Crear")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = tab == "Puntos fijos", onClick = { tab = "Puntos fijos" }, label = { Text("Puntos fijos") })
                FilterChip(selected = tab == "Eventos", onClick = { tab = "Eventos" }, label = { Text("Eventos de recojo") })
            }

            if (tab == "Puntos fijos") {
                if (puntos.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay puntos fijos registrados.")
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(puntos) { punto ->
                            PuntoCard(
                                punto = punto,
                                onEditar = { onEditarPunto(punto.id) },
                                onToggle = { viewModel.togglePunto(punto.id, punto.activo) }
                            )
                        }
                    }
                }
            } else {
                if (eventos.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay eventos de recojo registrados.")
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(eventos) { evento ->
                            EventoCard(
                                evento = evento,
                                onEditar = { onEditarEvento(evento.id) },
                                onToggle = { viewModel.toggleEvento(evento.id, evento.activo) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PuntoCard(punto: PuntoEntregaFirestore, onEditar: () -> Unit, onToggle: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(punto.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(punto.direccion, style = MaterialTheme.typography.bodySmall)
            Text("Horario: ${punto.horario}", style = MaterialTheme.typography.bodySmall)
            Text(
                text = if (punto.activo) "Activo" else "Inactivo",
                style = MaterialTheme.typography.labelMedium,
                color = if (punto.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
            Row {
                OutlinedButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, null, Modifier.width(18.dp)); Spacer(Modifier.width(4.dp)); Text("Editar")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = onToggle) {
                    Icon(if (punto.activo) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, Modifier.width(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (punto.activo) "Desactivar" else "Activar")
                }
            }
        }
    }
}

@Composable
private fun EventoCard(evento: PickupEventFirestore, onEditar: () -> Unit, onToggle: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(evento.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Campaña: ${evento.campaignTitulo}", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary)
            Text("${evento.lugar} - ${evento.direccion}", style = MaterialTheme.typography.bodySmall)
            Text("Fecha: ${evento.fecha} a las ${evento.hora}", style = MaterialTheme.typography.bodySmall)
            Text(
                text = if (evento.activo) "Activo" else "Inactivo",
                style = MaterialTheme.typography.labelMedium,
                color = if (evento.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
            Row {
                OutlinedButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, null, Modifier.width(18.dp)); Spacer(Modifier.width(4.dp)); Text("Editar")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = onToggle) {
                    Icon(if (evento.activo) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, Modifier.width(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (evento.activo) "Desactivar" else "Activar")
                }
            }
        }
    }
}
