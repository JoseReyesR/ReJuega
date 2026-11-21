// (nuevo) Archivo: PerfilScreen.kt
// (nuevo) Descripción: Pantalla de Perfil de Usuario (datos personales y contraseña).
package pe.rejuega.app.ui.screens

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.rejuega.app.viewmodel.PerfilUsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    viewModel: PerfilUsuarioViewModel,
    onBackClick: () -> Unit
) {
    val perfilState by viewModel.state.collectAsState()

    var mostrarDialogoContrasena by remember { mutableStateOf(false) }
    var contrasenaActual by remember { mutableStateOf("") }
    var contrasenaNueva by remember { mutableStateOf("") }
    var contrasenaConfirmacion by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(perfilState.mensaje, perfilState.error) {
        perfilState.mensaje?.let { snackbarHostState.showSnackbar(it); viewModel.limpiarMensaje() }
        perfilState.error?.let { snackbarHostState.showSnackbar(it); viewModel.limpiarMensaje() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mi Perfil",
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // (nuevo) Sección de datos personales del perfil.
            Text(
                "Mis datos de perfil",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = perfilState.nombre,
                onValueChange = { viewModel.onCampoChange("nombre", it) },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = perfilState.telefono,
                onValueChange = { viewModel.onCampoChange("telefono", it) },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = perfilState.ciudad,
                onValueChange = { viewModel.onCampoChange("ciudad", it) },
                label = { Text("Ciudad / distrito") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = perfilState.direccion,
                onValueChange = { viewModel.onCampoChange("direccion", it) },
                label = { Text("Dirección (referencia de recojo)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // (nuevo) Dropdown de género.
            var expandedGenero by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expandedGenero, onExpandedChange = { expandedGenero = it }) {
                OutlinedTextField(
                    value = perfilState.genero,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Género (opcional)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGenero) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expandedGenero, onDismissRequest = { expandedGenero = false }) {
                    listOf("Femenino", "Masculino", "No binario", "Prefiero no decir").forEach { g ->
                        DropdownMenuItem(
                            text = { Text(g) },
                            onClick = {
                                viewModel.onCampoChange("genero", g)
                                expandedGenero = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.guardarPerfil() },
                enabled = !perfilState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (perfilState.isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Guardar cambios de perfil")
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { mostrarDialogoContrasena = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cambiar contraseña") }
        }
    }

    // (nuevo) Diálogo de cambio de contraseña.
    if (mostrarDialogoContrasena) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoContrasena = false },
            title = { Text("Cambiar contraseña") },
            text = {
                Column {
                    OutlinedTextField(
                        value = contrasenaActual,
                        onValueChange = { contrasenaActual = it },
                        label = { Text("Contraseña actual") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contrasenaNueva,
                        onValueChange = { contrasenaNueva = it },
                        label = { Text("Nueva contraseña (mín. 6)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contrasenaConfirmacion,
                        onValueChange = { contrasenaConfirmacion = it },
                        label = { Text("Confirmar nueva") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cambiarContrasena(contrasenaActual, contrasenaNueva, contrasenaConfirmacion)
                    mostrarDialogoContrasena = false
                    contrasenaActual = ""; contrasenaNueva = ""; contrasenaConfirmacion = ""
                }) { Text("Actualizar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoContrasena = false }) { Text("Cancelar") }
            }
        )
    }
}
