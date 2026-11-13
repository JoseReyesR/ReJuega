// (modificado) Archivo: CampaignFormScreen.kt
// (modificado) Cambio: Se agrega DatePicker para las fechas y se corrige
// (modificado) la carga de datos al editar una campaña existente.
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
import pe.rejuega.app.ui.viewmodel.CampaignAdminViewModel
import pe.rejuega.app.utils.convertirFechaAMillis
import pe.rejuega.app.utils.convertirMillisAFecha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignFormScreen(
    viewModel: CampaignAdminViewModel,
    campaignId: String,
    onBackClick: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    // (nuevo) Observamos la lista de campañas para poder cargar los datos al editar.
    val campaigns by viewModel.campaigns.collectAsState()

    // (nuevo) Campos del formulario.
    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var fechaInicio by rememberSaveable { mutableStateOf("") }
    var fechaFin by rememberSaveable { mutableStateOf("") }
    var imagenUrl by rememberSaveable { mutableStateOf("") }

    // (nuevo) Controla qué DatePicker está visible: null, "inicio" o "fin".
    var mostrarDatePicker by remember { mutableStateOf<String?>(null) }

    // (nuevo) Bandera para cargar los datos solo una vez al editar.
    var datosCargados by remember { mutableStateOf(false) }

    // (modificado) Carga los datos cuando la lista de campañas esté disponible.
    // (modificado) Antes fallaba porque la lista aún no se había cargado de Firestore.
    LaunchedEffect(campaigns) {
        // (nuevo) Solo se ejecuta si es edición y los datos aún no se han cargado.
        if (campaignId != "new" && !datosCargados && campaigns.isNotEmpty()) {
            val campaign = campaigns.find { it.id == campaignId }
            campaign?.let { c ->
                titulo = c.titulo
                descripcion = c.descripcion
                fechaInicio = c.fechaInicio
                fechaFin = c.fechaFin
                imagenUrl = c.imagenUrl
                // (nuevo) Marcamos como cargado para no sobrescribir cambios del usuario.
                datosCargados = true
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
            // (nuevo) Campo título.
            OutlinedTextField(
                value = titulo, onValueChange = { titulo = it },
                label = { Text("Título") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            // (nuevo) Campo descripción.
            OutlinedTextField(
                value = descripcion, onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
            Spacer(Modifier.height(12.dp))

            // (modificado) Campo fecha inicio con DatePicker.
            OutlinedTextField(
                value = fechaInicio,
                onValueChange = { /* readOnly: no se edita manualmente */ },
                label = { Text("Fecha inicio") },
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

            // (modificado) Campo fecha fin con DatePicker.
            OutlinedTextField(
                value = fechaFin,
                onValueChange = { /* readOnly: no se edita manualmente */ },
                label = { Text("Fecha fin") },
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

            // (nuevo) Campo URL de imagen.
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

            // (nuevo) Botón guardar.
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
                    CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (campaignId == "new") "Crear Campaña" else "Guardar Cambios")
                }
            }
        }
    }

    // (nuevo) DatePickerDialog: se muestra cuando mostrarDatePicker no es null.
    if (mostrarDatePicker != null) {
        // (nuevo) Determina si es fecha inicio o fin.
        val esInicio = mostrarDatePicker == "inicio"
        // (nuevo) Fecha actual del campo (para abrir el DatePicker en esa fecha).
        val fechaActual = if (esInicio) fechaInicio else fechaFin
        val initialMillis = convertirFechaAMillis(fechaActual)

        // (nuevo) Estado del DatePicker con la fecha inicial.
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis
        )

        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        // (nuevo) Convierte los millis seleccionados a YYYY-MM-DD.
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val fecha = convertirMillisAFecha(millis)
                            if (esInicio) fechaInicio = fecha else fechaFin = fecha
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