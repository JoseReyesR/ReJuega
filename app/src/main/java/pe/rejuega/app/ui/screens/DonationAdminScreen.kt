// (nuevo) Pantalla de lista de donaciones pendientes para el Admin.
package pe.rejuega.app.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pe.rejuega.app.data.remote.Donacion
import pe.rejuega.app.ui.viewmodel.DonationAdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationAdminScreen(
    viewModel: DonationAdminViewModel,
    onBackClick: () -> Unit,
    onDonacionClick: (Donacion) -> Unit

) {
    val pendientes by viewModel.donations.collectAsState()
    val listaFiltrada = pendientes.filter {
        it.estadoTrazabilidad == "Pendiente" || it.estadoTrazabilidad == "En Revision"
    }

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
        if (listaFiltrada.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay donaciones pendientes de revisión.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listaFiltrada) { donacion ->
                    DonationCard(
                        donacion = donacion,
                        onClick = { onDonacionClick(donacion) }
                    )
                }
            }
        }
    }
}

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
                    color = if (donacion.estadoTrazabilidad == "Pendiente")
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}