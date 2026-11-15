// (nuevo) Formulario crear/editar punto fijo de la ONG.
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.rejuega.app.data.remote.PuntoEntregaFirestore
import pe.rejuega.app.ui.components.HorarioPicker
import pe.rejuega.app.ui.components.SelectorDireccion
import pe.rejuega.app.ui.components.construirHorario
import pe.rejuega.app.ui.viewmodel.PuntosRecojoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuntoEntregaFormScreen(
    viewModel: PuntosRecojoViewModel,
    puntoId: String,
    onBackClick: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val puntos by viewModel.puntos.collectAsState()
    val sugerencias by viewModel.sugerencias.collectAsState()
    val buscando by viewModel.buscando.collectAsState()

    var nombre by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    var dias by remember { mutableStateOf<List<String>>(emptyList()) }
    var horaInicio by rememberSaveable { mutableStateOf("") }
    var horaFin by rememberSaveable { mutableStateOf("") }
    var latitud by remember { mutableStateOf(0.0) }
    var longitud by remember { mutableStateOf(0.0) }
    var coordenadasListas by remember { mutableStateOf(false) }

    var errorLocal by remember { mutableStateOf<String?>(null) }
    var cargado by remember { mutableStateOf(false) }

    // (nuevo) Carga datos si es edición.
    LaunchedEffect(puntos) {
        if (puntoId != "new" && !cargado) {
            viewModel.puntoPorId(puntoId)?.let { p ->
                nombre = p.nombre
                direccion = p.direccion
                dias = p.dias
                horaInicio = p.horaInicio
                horaFin = p.horaFin
                latitud = p.latitud
                longitud = p.longitud
                if (latitud != 0.0 || longitud != 0.0) {
                    coordenadasListas = true
                }
                cargado = true
            }
        }
    }

    LaunchedEffect(uiState.mensaje) {
        if (uiState.mensaje != null) { viewModel.limpiarEstado(); onSaved() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (puntoId == "new") "Nuevo punto fijo" else "Editar punto fijo", fontWeight = FontWeight.Bold) },
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
            OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
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

            // (nuevo) Horario estructurado.
            HorarioPicker(
                diasSeleccionados = dias,
                onDiasChange = { dias = it },
                horaInicio = horaInicio,
                onHoraInicioChange = { horaInicio = it },
                horaFin = horaFin,
                onHoraFinChange = { horaFin = it }
            )

            Spacer(Modifier.height(16.dp))

            errorLocal?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    if (nombre.isBlank() || direccion.isBlank()) { errorLocal = "Nombre y dirección son obligatorios."; return@Button }
                    if (!coordenadasListas) { errorLocal = "Busca y selecciona una dirección para obtener coordenadas."; return@Button }
                    if (dias.isEmpty() || horaInicio.isBlank() || horaFin.isBlank()) { errorLocal = "Selecciona días y horario."; return@Button }
                    errorLocal = null
                    val existente = if (puntoId != "new") viewModel.puntoPorId(puntoId) else null
                    viewModel.guardarPunto(
                        PuntoEntregaFirestore(
                            id = puntoId.takeIf { it != "new" } ?: "",
                            nombre = nombre.trim(),
                            direccion = direccion.trim(),
                            horario = construirHorario(dias, horaInicio, horaFin),
                            dias = dias, horaInicio = horaInicio, horaFin = horaFin,
                            latitud = latitud, longitud = longitud,
                            activo = existente?.activo ?: true,
                            creadoPor = existente?.creadoPor ?: ""
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar punto") }
        }
    }
}
