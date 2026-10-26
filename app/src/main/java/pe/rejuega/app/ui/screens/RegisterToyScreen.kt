// (nuevo) Archivo: RegisterToyScreen.kt
// (nuevo) Descripción: Pantalla para registrar una propuesta de juguete.
// (nuevo) Implementa validaciones de la regla de negocio RB-01 y advertencia RB-06.
package pe.rejuega.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pe.rejuega.app.ui.viewmodel.JugueteViewModel

// (nuevo) OptIn requerido por TopAppBar de Material 3.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterToyScreen(
    viewModel: JugueteViewModel,
    onBackClick: () -> Unit,
    onSaved: () -> Unit
) {
    // (nuevo) Observamos el estado del formulario.
    val uiState by viewModel.uiState.collectAsState()

    // (nuevo) Launcher para seleccionar una imagen desde la galería.
    // (nuevo) No requiere permiso complicado de cámara por ahora.
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onFotoSeleccionada(uri)
    }

    // (nuevo) Cuando el guardado sea exitoso, regresamos a la pantalla anterior.
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Registrar juguete",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // (nuevo) Texto introductorio.
            Text(
                text = "Completa los datos del juguete que deseas donar.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // (nuevo) Campo nombre.
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = viewModel::onNombreChange,
                label = { Text("Nombre del juguete") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // (nuevo) Campo descripción.
            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = viewModel::onDescripcionChange,
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // (nuevo) Selección de estado del juguete.
            Text(
                text = "Estado del juguete",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = uiState.estado == "Nuevo",
                    onClick = { viewModel.onEstadoChange("Nuevo") }
                )
                Text(text = "Nuevo")

                Spacer(modifier = Modifier.size(16.dp))

                RadioButton(
                    selected = uiState.estado == "Buen Estado",
                    onClick = { viewModel.onEstadoChange("Buen Estado") }
                )
                Text(text = "Buen Estado")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // (nuevo) Botón para seleccionar foto.
            Button(
                onClick = {
                    imagePickerLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Seleccionar fotografía")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // (nuevo) Vista previa de la imagen seleccionada.
            if (uiState.fotoUri != null) {
                AsyncImage(
                    model = uiState.fotoUri,
                    contentDescription = "Foto seleccionada del juguete",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "No has seleccionado ninguna fotografía.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // (nuevo) Advertencia de privacidad RB-06.
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Importante: La fotografía no debe mostrar rostros de menores de edad. Esta advertencia cumple con la regla de negocio RB-06 y la política de privacidad del proyecto.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // (nuevo) Mostrar error de validación si existe.
            if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // (nuevo) Botón para registrar el juguete.
            Button(
                onClick = viewModel::registrarJuguete,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Registrar propuesta de donación")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}