// (nuevo) Pantalla que muestra los códigos HTTP del servidor propio en pantalla.
package pe.rejuega.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.rejuega.app.domain.model.PruebaApi
import pe.rejuega.app.viewmodel.ApiTestViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// (nuevo) Color según el código HTTP.
private fun colorDeCodigo(codigo: Int): Color = when (codigo) {
    in 200..299 -> Color(0xFF2E7D32)   // verde: éxito
    in 400..499 -> Color(0xFFEF6C00)   // naranja: error del cliente
    else -> Color(0xFFC62828)          // rojo: servidor / sin conexión
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiTestScreen(
    viewModel: ApiTestViewModel,
    onBackClick: () -> Unit
) {
    val pruebas by viewModel.pruebas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pruebas del Servidor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.limpiar() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // (nuevo) Panel de botones de prueba.
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text("Endpoints de prueba", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))

                Button(onClick = { viewModel.ejecutarTodas() }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
                    Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text("Ejecutar todas las pruebas")
                }
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.probarGet200() }, modifier = Modifier.weight(1f), enabled = !isLoading) { Text("GET 200", maxLines = 1) }
                    OutlinedButton(onClick = { viewModel.probarGet404() }, modifier = Modifier.weight(1f), enabled = !isLoading) { Text("GET 404", maxLines = 1) }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.probarPost201() }, modifier = Modifier.weight(1f), enabled = !isLoading) { Text("POST 201", maxLines = 1) }
                    OutlinedButton(onClick = { viewModel.probarPost400() }, modifier = Modifier.weight(1f), enabled = !isLoading) { Text("POST 400", maxLines = 1) }
                }

                if (isLoading) {
                    Spacer(Modifier.height(12.dp))
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                }
            }

            // (nuevo) Historial de resultados con el código visible.
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pruebas) { prueba ->
                    PruebaCard(prueba)
                }
            }
        }
    }
}

@Composable
private fun PruebaCard(prueba: PruebaApi) {
    val formato = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // (nuevo) Código HTTP grande y con color.
            Text(
                text = if (prueba.codigo == 0) "OFF" else prueba.codigo.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorDeCodigo(prueba.codigo)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(prueba.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("${prueba.metodo} ${prueba.url}", style = MaterialTheme.typography.bodySmall)
                Text(prueba.detalle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formato.format(Date(prueba.fecha)), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
