// (modificado) Archivo: CampaignFormScreen.kt
// (modificado) Cambio: Se agrega DatePicker con fechas visibles dd/MM/yyyy y conversión a YYYY-MM-DD para MySQL.
package pe.rejuega.app.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.rejuega.app.utils.convertirFechaAMillis
import pe.rejuega.app.utils.convertirMillisAFecha
import pe.rejuega.app.utils.fechaVisibleAIso
import pe.rejuega.app.utils.isoAFechaVisible
import pe.rejuega.app.utils.RefreshTrigger
import pe.rejuega.app.viewmodel.CampaignAdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignFormScreen(
    viewModel: CampaignAdminViewModel,
    campaignId: String,
    onBackClick: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val campaigns by viewModel.campaigns.collectAsState()

    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var fechaInicio by rememberSaveable { mutableStateOf("") }
    var fechaFin by rememberSaveable { mutableStateOf("") }
    var imagenUrl by rememberSaveable { mutableStateOf("") }

    var mostrarDatePicker by remember { mutableStateOf<String?>(null) }
    var datosCargados by remember { mutableStateOf(false) }

    LaunchedEffect(campaigns) {
        if (campaignId != "new" && !datosCargados && campaigns.isNotEmpty()) {
            val campaign = campaigns.find { it.id == campaignId }
            campaign?.let { c ->
                titulo = c.titulo
                descripcion = c.descripcion
                fechaInicio = isoAFechaVisible(c.fechaInicio)
                fechaFin = isoAFechaVisible(c.fechaFin)
                imagenUrl = c.imagenUrl
                datosCargados = true
            }
        }
    }

    LaunchedEffect(uiState.mensajeExito) {
        if (uiState.mensajeExito != null) {
            println(">>> REFRESH-B: formulario guardó con éxito, notifico")
            RefreshTrigger.notificar()
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
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = fechaInicio,
                onValueChange = { fechaInicio = it },
                label = { Text("Fecha inicio (dd/MM/yyyy)") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { mostrarDatePicker = "inicio" }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha inicio")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarDatePicker = "inicio" }
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = fechaFin,
                onValueChange = { fechaFin = it },
                label = { Text("Fecha fin (dd/MM/yyyy)") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { mostrarDatePicker = "fin" }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha fin")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarDatePicker = "fin" }
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = imagenUrl, onValueChange = { imagenUrl = it },
                label = { Text("URL de imagen") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            if (uiState.error != null) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    val fInicioIso = fechaVisibleAIso(fechaInicio)
                    val fFinIso = fechaVisibleAIso(fechaFin)
                    if (campaignId == "new") {
                        viewModel.crearCampaign(titulo, descripcion, fInicioIso, fFinIso, imagenUrl)
                    } else {
                        val existente = viewModel.obtenerCampaignPorId(campaignId)
                        if (existente != null) {
                            viewModel.actualizarCampaign(
                                existente.copy(
                                    titulo = titulo, descripcion = descripcion,
                                    fechaInicio = fInicioIso, fechaFin = fFinIso,
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
                    CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (campaignId == "new") "Crear Campaña" else "Guardar Cambios")
                }
            }
        }
    }

    if (mostrarDatePicker != null) {
        val esInicio = mostrarDatePicker == "inicio"
        val fechaActualVisible = if (esInicio) fechaInicio else fechaFin
        val fechaIso = fechaVisibleAIso(fechaActualVisible)
        val initialMillis = convertirFechaAMillis(fechaIso)

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis
        )

        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val fechaIsoSelec = convertirMillisAFecha(millis)
                            val fechaVis = isoAFechaVisible(fechaIsoSelec)
                            if (esInicio) fechaInicio = fechaVis else fechaFin = fechaVis
                        }
                        mostrarDatePicker = null
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = null }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
