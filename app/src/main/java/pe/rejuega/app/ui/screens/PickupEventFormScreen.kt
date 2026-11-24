// (nuevo) Formulario crear/editar evento de recojo masivo por campaña.
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import pe.rejuega.app.data.remote.PickupEventFirestore
import pe.rejuega.app.ui.components.SelectorDireccion
import pe.rejuega.app.ui.components.TimeDropdown
import pe.rejuega.app.utils.RefreshTrigger
import pe.rejuega.app.viewmodel.PuntosRecojoViewModel
import pe.rejuega.app.utils.convertirFechaAMillis
import pe.rejuega.app.utils.convertirMillisAFecha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickupEventFormScreen(
    viewModel: PuntosRecojoViewModel,
    eventId: String,
    onBackClick: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val eventos by viewModel.eventos.collectAsState()
    val campaigns by viewModel.campaigns.collectAsState()
    val sugerencias by viewModel.sugerencias.collectAsState()
    val buscando by viewModel.buscando.collectAsState()

    var campaignId by rememberSaveable { mutableStateOf("") }
    var campaignTitulo by rememberSaveable { mutableStateOf("") }
    var nombre by rememberSaveable { mutableStateOf("") }
    var lugar by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf("") }
    var hora by rememberSaveable { mutableStateOf("") }
    var latitud by remember { mutableStateOf(0.0) }
    var longitud by remember { mutableStateOf(0.0) }
    var coordenadasListas by remember { mutableStateOf(false) }

    var errorLocal by remember { mutableStateOf<String?>(null) }
    var cargado by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var mostrarDatePicker by remember { mutableStateOf(false) }

    // (nuevo) Carga datos si es edición.
    LaunchedEffect(eventos) {
        if (eventId != "new" && !cargado) {
            viewModel.eventoPorId(eventId)?.let { e ->
                campaignId = e.campaignId; campaignTitulo = e.campaignTitulo
                nombre = e.nombre; lugar = e.lugar; direccion = e.direccion
                fecha = e.fecha; hora = e.hora
                latitud = e.latitud; longitud = e.longitud
                if (latitud != 0.0 || longitud != 0.0) {
                    coordenadasListas = true
                }
                cargado = true
            }
        }
    }

    LaunchedEffect(uiState.mensaje) {
        if (uiState.mensaje != null) {
            RefreshTrigger.notificar()
            viewModel.limpiarEstado()
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (eventId == "new") "Nuevo evento de recojo" else "Editar evento", fontWeight = FontWeight.Bold) },
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
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            // (nuevo) Selector de campaña (dropdown).
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = campaignTitulo,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Campaña") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    campaigns.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.titulo) },
                            onClick = { campaignId = c.id; campaignTitulo = c.titulo; expanded = false }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre del evento") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(lugar, { lugar = it }, label = { Text("Lugar (ej. Parque Central)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            // (nuevo) Selector de dirección con geocodificación.
            SelectorDireccion(
                direccion = direccion,
                onDireccionChange = { direccion = it },
                buscando = buscando,
                sugerencias = sugerencias,
                onBuscar = { viewModel.buscarDireccion(direccion) },
                onSeleccionar = { resultado ->
                    latitud = resultado.lat.toDoubleOrNull() ?: 0.0
                    longitud = resultado.lon.toDoubleOrNull() ?: 0.0
                    coordenadasListas = true
                    direccion = resultado.display_name.substringBefore(",")
                    viewModel.limpiarSugerencias()
                }
            )

            if (coordenadasListas) {
                Spacer(Modifier.height(4.dp))
                Text("Coordenadas: $latitud, $longitud",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(12.dp))

            // (nuevo) Fecha con DatePicker.
            OutlinedTextField(
                value = fecha, onValueChange = {}, readOnly = true,
                label = { Text("Fecha") },
                trailingIcon = { IconButton(onClick = { mostrarDatePicker = true }) { Icon(Icons.Default.DateRange, null) } },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            // (nuevo) Hora con TimeDropdown.
            TimeDropdown(
                label = "Hora",
                selected = hora,
                onSelect = { hora = it }
            )

            Spacer(Modifier.height(16.dp))

            errorLocal?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    if (campaignId.isBlank()) { errorLocal = "Selecciona una campaña."; return@Button }
                    if (nombre.isBlank() || lugar.isBlank() || direccion.isBlank()) { errorLocal = "Completa nombre, lugar y dirección."; return@Button }
                    if (!coordenadasListas) { errorLocal = "Busca y selecciona una dirección para obtener coordenadas."; return@Button }
                    if (fecha.isBlank() || hora.isBlank()) { errorLocal = "Indica fecha y hora."; return@Button }
                    errorLocal = null
                    val existente = if (eventId != "new") viewModel.eventoPorId(eventId) else null
                    viewModel.guardarEvento(
                        PickupEventFirestore(
                            id = eventId.takeIf { it != "new" } ?: "",
                            campaignId = campaignId, campaignTitulo = campaignTitulo,
                            nombre = nombre.trim(), lugar = lugar.trim(), direccion = direccion.trim(),
                            fecha = fecha, hora = hora.trim(), latitud = latitud, longitud = longitud,
                            activo = existente?.activo ?: true,
                            creadoPor = existente?.creadoPor ?: ""
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar evento") }
        }
    }

    // (nuevo) DatePicker para la fecha del evento.
    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = convertirFechaAMillis(fecha))
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { fecha = convertirMillisAFecha(it) }
                    mostrarDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }
}
