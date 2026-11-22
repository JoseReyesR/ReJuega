// (nuevo) Pantalla de gestión de usuarios: lista + cambio de rol (solo Admin ve el selector).
package pe.rejuega.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.rejuega.app.data.remote.UserNodeDto
import pe.rejuega.app.viewmodel.GestionUsuariosViewModel

private fun colorRol(rol: String) = when (rol) {
    "Administrador" -> Color(0xFFC62828)
    "Gestor" -> Color(0xFFEF6C00)
    else -> Color(0xFF2E7D32)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    viewModel: GestionUsuariosViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.mensaje, state.error) {
        state.mensaje?.let { snackbarHostState.showSnackbar(it); viewModel.limpiarMensaje() }
        state.error?.let { snackbarHostState.showSnackbar(it); viewModel.limpiarMensaje() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios", fontWeight = FontWeight.Bold) },
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
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
                    item {
                        Text(
                            if (state.puedeCambiarRoles)
                                "Toca un rol para cambiarlo (solo Administrador)"
                            else
                                "Solo el Administrador puede cambiar roles",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(state.usuarios) { usuario ->
                        UsuarioCard(
                            usuario = usuario,
                            puedeCambiar = state.puedeCambiarRoles,
                            onRolChange = { nuevoRol -> viewModel.cambiarRol(usuario.firebaseUid, nuevoRol) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UsuarioCard(
    usuario: UserNodeDto,
    puedeCambiar: Boolean,
    onRolChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    usuario.nombre.ifBlank { "Sin nombre" },
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold
                )
                Text(usuario.correo, style = MaterialTheme.typography.bodySmall)
            }

            if (puedeCambiar) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = usuario.rol, onValueChange = {}, readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.width(160.dp).menuAnchor(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = colorRol(usuario.rol),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("Donante", "Gestor", "Administrador").forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = { onRolChange(opcion); expanded = false }
                            )
                        }
                    }
                }
            } else {
                Text(
                    usuario.rol,
                    color = colorRol(usuario.rol),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
