// (nuevo) Pantalla para editar una donación Pendiente.
package pe.rejuega.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.rejuega.app.viewmodel.EditarDonacionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarDonacionScreen(
    viewModel: EditarDonacionViewModel,
    uid: String,
    campaignId: Long,
    campaignTitulo: String,
    fotoUrl: String,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var expandedMaterial by remember { mutableStateOf(false) }

    LaunchedEffect(state.mensaje) {
        if (state.mensaje != null) {
            viewModel.limpiarMensaje()
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar donación", fontWeight = FontWeight.Bold) },
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
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                OutlinedTextField(
                    value = state.nombre,
                    onValueChange = { viewModel.onNombre(it) },
                    label = { Text("Nombre del juguete") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.descripcion,
                    onValueChange = { viewModel.onDescripcion(it) },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                Text("Condición", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Row {
                    listOf("Nuevo", "Buen Estado").forEach { op ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = state.condicion == op,
                                onClick = { viewModel.onCondicion(op) }
                            )
                            Text(op)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                Text("Material", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                ExposedDropdownMenuBox(expanded = expandedMaterial, onExpandedChange = { expandedMaterial = it }) {
                    OutlinedTextField(
                        value = state.materialTipo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Material") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedMaterial) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandedMaterial, onDismissRequest = { expandedMaterial = false }) {
                        state.materiales.forEach { m ->
                            DropdownMenuItem(
                                text = { Text("${m.tipoMaterial} · ${m.co2AhorradoKg} kg") },
                                onClick = { viewModel.onMaterial(m); expandedMaterial = false }
                            )
                        }
                    }
                }

                if (state.error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { viewModel.guardar(uid, campaignId, campaignTitulo, fotoUrl) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Guardar cambios") }
            }
        }
    }
}
