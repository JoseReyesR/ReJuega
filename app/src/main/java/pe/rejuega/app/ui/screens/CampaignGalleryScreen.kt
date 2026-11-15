// (nuevo) Galería de evidencias de una campaña.
// (nuevo) Admin ve botón de subir y puede eliminar; donante solo ve.
package pe.rejuega.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pe.rejuega.app.data.remote.CampaignEvidence
import pe.rejuega.app.viewmodel.CampaignGalleryViewModel
import pe.rejuega.app.utils.formatarFechaHora

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignGalleryScreen(
    viewModel: CampaignGalleryViewModel,
    esAdmin: Boolean,
    onBackClick: () -> Unit
) {
    val evidencias by viewModel.evidencias.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var mostrarDialogo by remember { mutableStateOf(false) }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var urisSeleccionadas by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val context = LocalContext.current

    // (nuevo) Launcher de selección múltiple de imágenes.
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        urisSeleccionadas = uris
        if (uris.isNotEmpty()) mostrarDialogo = true
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Galería de evidencias", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            // (nuevo) Solo el Admin ve el botón flotante de agregar.
            if (esAdmin) {
                FloatingActionButton(onClick = { launcher.launch("image/*") }) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Agregar evidencias")
                }
            }
        }
    ) { padding ->
        if (evidencias.isEmpty() && !isUploading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Aún no hay evidencias en esta campaña.")
                    if (esAdmin) {
                        Spacer(Modifier.height(4.dp))
                        Text("Toca el botón + para agregar fotos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(evidencias) { evidencia ->
                    EvidenceCard(
                        evidencia = evidencia,
                        esAdmin = esAdmin,
                        onEliminar = { viewModel.eliminarEvidencia(evidencia.id) }
                    )
                }
            }
        }

        // (nuevo) Indicador de subida.
        if (isUploading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Card(
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Subiendo evidencias...")
                    }
                }
            }
        }
    }

    // (nuevo) Diálogo de confirmación con descripción.
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogo = false
                urisSeleccionadas = emptyList()
                descripcion = ""
            },
            title = { Text("Subir ${urisSeleccionadas.size} imágenes") },
            text = {
                Column {
                    Text("Agrega una descripción para todas las imágenes:")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.subirEvidencias(context, urisSeleccionadas, descripcion)
                        mostrarDialogo = false
                        urisSeleccionadas = emptyList()
                        descripcion = ""
                    },
                    enabled = descripcion.isNotBlank()
                ) { Text("Subir") }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    urisSeleccionadas = emptyList()
                    descripcion = ""
                }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun EvidenceCard(
    evidencia: CampaignEvidence,
    esAdmin: Boolean,
    onEliminar: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = evidencia.fotoUrl,
                contentDescription = evidencia.descripcion,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(8.dp)) {
                Text(evidencia.descripcion.ifBlank { "Sin descripción" },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2)
                Text(formatarFechaHora(evidencia.fechaSubida),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (esAdmin) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(Modifier.width(4.dp))
                        Text("Admin", style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onEliminar, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
