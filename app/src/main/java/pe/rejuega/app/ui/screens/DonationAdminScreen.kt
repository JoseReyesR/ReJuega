// (modificado) Pantalla de revisión con filtros por estado y agrupación por campaña.
// (modificado) Motivo: las donaciones Entregadas/Rechazadas no deben "desaparecer",
// (modificado) y el Admin necesita verlas organizadas por campaña.
package pe.rejuega.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pe.rejuega.app.data.remote.Donacion
import pe.rejuega.app.ui.viewmodel.DonationAdminViewModel

// (nuevo) Opciones de filtro disponibles.
private val FILTROS = listOf("Por revisar", "Aprobadas", "Entregadas", "Rechazadas", "Todas")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationAdminScreen(
    viewModel: DonationAdminViewModel,
    onBackClick: () -> Unit,
    onDonacionClick: (Donacion) -> Unit
) {
    val donations by viewModel.donations.collectAsState()
    // (nuevo) Filtro activo; por defecto la cola de revisión.
    var filtro by rememberSaveable { mutableStateOf("Por revisar") }

    // (nuevo) Filtrado por estado. "Todas" muestra el historial completo:
    // (nuevo) nada desaparece, solo cambia la vista.
    val filtradas = donations.filter { d ->
        when (filtro) {
            "Por revisar" -> d.estadoTrazabilidad == "Pendiente" || d.estadoTrazabilidad == "En Revision"
            "Aprobadas" -> d.estadoTrazabilidad == "Aprobado"
            "Entregadas" -> d.estadoTrazabilidad == "Entregado"
            "Rechazadas" -> d.estadoTrazabilidad == "Rechazado"
            else -> true
        }
    }

    // (nuevo) Agrupación por campaña usando el título desnormalizado.
    val grupos = filtradas.groupBy { it.campaignTitulo.ifBlank { "Sin campaña asignada" } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revisar Donaciones", fontWeight = FontWeight.Bold) },
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
        Column(Modifier.fillMaxSize().padding(padding)) {

            // (nuevo) Fila de chips de filtro, con scroll horizontal si no caben.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FILTROS.forEach { opcion ->
                    FilterChip(
                        selected = filtro == opcion,
                        onClick = { filtro = opcion },
                        label = { Text(opcion) }
                    )
                }
            }

            if (filtradas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay donaciones en este filtro.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // (nuevo) Por cada campaña: un encabezado y sus donaciones debajo.
                    grupos.forEach { (campana, lista) ->
                        item {
                            Text(
                                text = "Campaña: $campana  (${lista.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(lista) { donacion ->
                            DonationCard(
                                donacion = donacion,
                                onClick = { onDonacionClick(donacion) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// (modificado) Tarjeta de donación (sin cambios de contenido, solo se mantiene).
@Composable
private fun DonationCard(donacion: Donacion, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = donacion.fotoUrl,
                contentDescription = donacion.nombre,
                modifier = Modifier.size(80.dp).clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(donacion.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(donacion.descripcion, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Estado: ${donacion.estadoTrazabilidad}",
                    style = MaterialTheme.typography.labelMedium,
                    color = when (donacion.estadoTrazabilidad) {
                        "Pendiente" -> MaterialTheme.colorScheme.primary
                        "Entregado" -> MaterialTheme.colorScheme.secondary
                        "Rechazado" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.tertiary
                    }
                )
            }
        }
    }
}