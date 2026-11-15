// (nuevo) Archivo: PanelAdminScreen.kt
// (nuevo) Descripción: Pantalla principal del Panel de Administración.
// (nuevo) Muestra las opciones disponibles para el rol Administrador.
// (nuevo) Cada opción se conectará a su funcionalidad en pasos posteriores.
// (nuevo) Implementa RN-04: esta pantalla solo es accesible para Administradores.
package pe.rejuega.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// (nuevo) Data class que representa una opción del panel de administración.
// (nuevo) Cada opción tiene un ícono, título, descripción y un mensaje de destino.
private data class AdminOption(
    val icono: ImageVector,
    val titulo: String,
    val descripcion: String,
    val destino: String
)

// (nuevo) Lista de opciones del Panel Admin.
// (nuevo) Cada una se implementará en un paso posterior.
private val adminOptions = listOf(
    AdminOption(
        icono = Icons.Default.Campaign,
        titulo = "Gestionar Campañas",
        descripcion = "Crear, editar y cerrar campañas de donación.",
        destino = "Se implementará en el Paso 13 (HU-03)."
    ),
    AdminOption(
        icono = Icons.Default.CheckCircle,
        titulo = "Revisar Donaciones",
        descripcion = "Validar juguetes propuestos y cambiar su estado.",
        destino = "Se implementará en el Paso 14 (HU-06)."
    ),
    AdminOption(
        icono = Icons.Default.Place,
        titulo = "Puntos de Recojo",
        descripcion = "Crear y gestionar puntos de entrega y eventos de recojo.",
        destino = "Se implementará en el Paso 18 (HU-04, HU-14)."
    ),
    AdminOption(
        icono = Icons.Default.PhotoLibrary,
        titulo = "Galería de Evidencias",
        descripcion = "Subir fotos de entrega de las campañas cerradas.",
        destino = "Se implementará en el Paso 17 (HU-09)."
    )
)

// (nuevo) OptIn necesario para TopAppBar de Material 3.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelAdminScreen(
    nombreAdmin: String,
    onBackClick: () -> Unit,
    //Firma de PanelAdminScreen: agregar onGestionarCampaigns.
    onGestionarCampaigns: () -> Unit = {},
    onRevisarDonaciones: () -> Unit = {},
    onGaleriaEvidencias: () -> Unit = onGestionarCampaigns
) {
    // (nuevo) Estado para mostrar Snackbars informativos.
    val snackbarHostState = remember { SnackbarHostState() }
    // (nuevo) Scope para lanzar corrutinas desde la UI (mostrar Snackbar).
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Panel de Administración",
                            fontWeight = FontWeight.Bold
                        )
                        // (nuevo) Mostramos el nombre del admin para confirmar el rol.
                        Text(
                            text = "Bienvenido, $nombreAdmin",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // (nuevo) Texto introductorio del panel.
            Text(
                text = "Selecciona una opción para gestionar la ONG.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))
            // aqui ponemos las opciones onclick del menu
            // (nuevo) Renderizamos cada opción como una tarjeta.
            adminOptions.forEach { opcion ->
                AdminOptionCard(
                    opcion = opcion,
                    onClick = {
                        when (opcion.titulo) {
                            "Gestionar Campañas" -> onGestionarCampaigns()
                            "Galería de Evidencias" -> onGaleriaEvidencias()
                            "Revisar Donaciones" -> onRevisarDonaciones()
                            else -> scope.launch { snackbarHostState.showSnackbar(opcion.destino) }
                        }
                    }
                )
            }
        }
    }
}

// (nuevo) Tarjeta individual para cada opción del panel.
@Composable
private fun AdminOptionCard(
    opcion: AdminOption,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // (nuevo) Ícono de la opción.
            Icon(
                imageVector = opcion.icono,
                contentDescription = opcion.titulo,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            // (nuevo) Título y descripción.
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = opcion.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = opcion.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}