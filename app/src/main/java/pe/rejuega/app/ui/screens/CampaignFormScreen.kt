// (nuevo) Formulario para crear o editar una campaña.
package pe.rejuega.app.ui.screens

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.rejuega.app.ui.viewmodel.CampaignAdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignFormScreen(
    viewModel: CampaignAdminViewModel,
    campaignId: String,   // (nuevo) "new" para crear, ID real para editar.
    onBackClick: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // (nuevo) Campos del formulario.
    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var fechaInicio by rememberSaveable { mutableStateOf("") }
    var fechaFin by rememberSaveable { mutableStateOf("") }
    var imagenUrl by rememberSaveable { mutableStateOf("") }

    // (nuevo) Si es edición, carga los datos existentes.
    LaunchedEffect(campaignId) {
        if (campaignId != "new") {
            viewModel.obtenerCampaignPorId(campaignId)?.let { c ->
                titulo = c.titulo
                descripcion = c.descripcion
                fechaInicio = c.fechaInicio
                fechaFin = c.fechaFin
                imagenUrl = c.imagenUrl
            }
        }
    }

    // (nuevo) Navega de vuelta al guardar con éxito.
    LaunchedEffect(uiState.mensajeExito) {
        if (uiState.mensajeExito != null) {
            viewModel.limpiarEstado()
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (campaignId == "new") "Crear Campaña" else "Editar Campaña",
                        fontWeight = FontWeight.Bold
                    )
                },
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
            OutlinedTextField(
                value = titulo, onValueChange = { titulo = it },
                label = { Text("Título") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = descripcion, onValueChange = { descripcion = it },
                label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth().height(100.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = fechaInicio, onValueChange = { fechaInicio = it },
                label = { Text("Fecha inicio (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = fechaFin, onValueChange = { fechaFin = it },
                label = { Text("Fecha fin (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = imagenUrl, onValueChange = { imagenUrl = it },
                label = { Text("URL de imagen") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // (nuevo) Muestra error si existe.
            if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    if (campaignId == "new") {
                        viewModel.crearCampaign(titulo, descripcion, fechaInicio, fechaFin, imagenUrl)
                    } else {
                        val existente = viewModel.obtenerCampaignPorId(campaignId)
                        if (existente != null) {
                            viewModel.actualizarCampaign(
                                existente.copy(
                                    titulo = titulo, descripcion = descripcion,
                                    fechaInicio = fechaInicio, fechaFin = fechaFin,
                                    imagenUrl = imagenUrl
                                )
                            )
                        }
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(Modifier.height(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (campaignId == "new") "Crear Campaña" else "Guardar Cambios")
                }
            }
        }
    }
}