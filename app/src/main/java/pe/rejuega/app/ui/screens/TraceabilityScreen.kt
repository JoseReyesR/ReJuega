// (nuevo) Archivo: TraceabilityScreen.kt
// (nuevo) Descripción: Pantalla de trazabilidad de donaciones (HU-06).
// (nuevo) Muestra loading, lista, estado vacío, error y acción simulada de administrador.
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pe.rejuega.app.data.local.Juguete
import pe.rejuega.app.ui.components.TrazabilidadTimeline
import pe.rejuega.app.ui.viewmodel.TraceabilityViewModel
import pe.rejuega.app.utils.ConfiguracionSimulacion
import pe.rejuega.app.utils.EstadosTrazabilidad

// (nuevo) OptIn necesario para TopAppBar de Material 3.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraceabilityScreen(
    viewModel: TraceabilityViewModel,
    onBackClick: () -> Unit
) {
    // (nuevo) Observamos los tres flujos que expone el ViewModel.
    val juguetes by viewModel.juguetes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()

    // (nuevo) Snackbar para mostrar errores sin bloquear la pantalla.
    val snackbarHostState = remember { SnackbarHostState() }

    // (nuevo) Cuando cambia el error, lo mostramos y luego limpiamos el estado.
    LaunchedEffect(mensajeError) {
        if (mensajeError != null) {
            snackbarHostState.showSnackbar(mensajeError ?: "")
            viewModel.limpiarError()
        }
    }

    Scaffold(
        // (nuevo) Contenedor de mensajes flotantes.
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis donaciones",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al inicio"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // (nuevo) Caso 1: todavía estamos cargando la BD.
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // (nuevo) Caso 2: la BD respondió pero no hay donaciones.
                juguetes.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Aún no has registrado juguetes.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Explora una campaña y haz tu primera donación.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // (nuevo) Caso 3: sí hay datos, mostramos la lista.
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(juguetes) { juguete ->
                            JugueteTraceabilityCard(
                                juguete = juguete,
                                onAvanzarEstado = { viewModel.avanzarEstado(juguete.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// (nuevo) Tarjeta individual de una donación con su línea de tiempo.
@Composable
private fun JugueteTraceabilityCard(
    juguete: Juguete,
    onAvanzarEstado: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // (nuevo) Cabecera: foto pequeña + nombre + campaña.
            Row(verticalAlignment = Alignment.CenterVertically) {

                AsyncImage(
                    model = juguete.fotoUrl,
                    contentDescription = "Foto del juguete ${juguete.nombre}",
                    modifier = Modifier
                        .size(64.dp)
                        .padding(end = 4.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = juguete.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Campaña N° ${juguete.campaignId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Condición: ${juguete.estado}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // (nuevo) Estado actual en texto (fuente única: EstadosTrazabilidad).
            Text(
                text = "Estado actual: ${juguete.estadoTrazabilidad.replace("En Revision", "En revisión")}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // (nuevo) Componente reutilizable de línea de tiempo.
            TrazabilidadTimeline(estadoActual = juguete.estadoTrazabilidad)

            // (nuevo) RB-03: la acción de avanzar estado SOLO aparece para Admin.
            // (nuevo) Hoy es una simulación; en Sprint 5 se valida con Firebase.
            if (ConfiguracionSimulacion.ES_ADMIN_SIMULADO &&
                !EstadosTrazabilidad.estaFinalizado(juguete.estadoTrazabilidad)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onAvanzarEstado,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Avanzar estado (Admin simulado)")
                }

                // (nuevo) Recordatorio visible de que esto no es seguridad real.
                Text(
                    text = "Simulación de desarrollo. En producción este botón requiere rol Administrador autenticado.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}