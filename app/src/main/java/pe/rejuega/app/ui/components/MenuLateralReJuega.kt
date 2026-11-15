// (nuevo) Menú lateral con opciones por rol (reemplaza los íconos saturados del TopAppBar).
package pe.rejuega.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// (nuevo) Cada opción del menú: id interno, nombre visible e ícono.
private data class DestinoMenu(
    val id: String,
    val nombre: String,
    val icono: ImageVector
)

@Composable
fun MenuLateralReJuega(
    nombreUsuario: String,
    esAdmin: Boolean,
    notificacionesNoLeidas: Int,
    onSeleccion: (String) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // (nuevo) Encabezado: marca + usuario + rol.
        Column(Modifier.padding(24.dp)) {
            Text(
                text = "ReJuega",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(nombreUsuario.ifBlank { "Usuario" }, style = MaterialTheme.typography.titleMedium)
            Text(
                text = if (esAdmin) "Administrador" else "Donante",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        HorizontalDivider()

        // (nuevo) Opciones según rol: el Admin ve gestión; el Donante ve su módulo.
        val destinos = buildList {
            if (esAdmin) {
                add(DestinoMenu("panel", "Panel de Administración", Icons.Default.AdminPanelSettings))
                add(DestinoMenu("campaigns", "Gestión de Campañas", Icons.Default.Campaign))
                add(DestinoMenu("donations", "Revisión de Donaciones", Icons.Default.CheckCircle))
                add(DestinoMenu("puntos", "Gestión de Puntos de Recojo", Icons.Default.Place))
            }
            add(DestinoMenu("trazabilidad", "Mis Donaciones", Icons.Default.History))
            add(DestinoMenu("mapa", "Mapa de Puntos de Recojo", Icons.Default.Map))
            if (!esAdmin) {
                add(DestinoMenu("impacto", "Impacto Ambiental", Icons.Default.Eco))
            }
            add(
                DestinoMenu(
                    "notificaciones",
                    if (notificacionesNoLeidas > 0) "Notificaciones ($notificacionesNoLeidas)" else "Notificaciones",
                    Icons.Default.Notifications
                )
            )
            add(DestinoMenu("perfil", "Mi Perfil", Icons.Default.Person))
        }

        destinos.forEach { destino ->
            NavigationDrawerItem(
                label = { Text(destino.nombre) },
                icon = { Icon(destino.icono, contentDescription = null) },
                selected = false,
                onClick = { onSeleccion(destino.id) },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        HorizontalDivider()

        // (nuevo) Cierre de sesión al final del menú.
        NavigationDrawerItem(
            label = { Text("Cerrar sesión") },
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
            selected = false,
            onClick = { onSeleccion("logout") },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            colors = NavigationDrawerItemDefaults.colors(
                selectedTextColor = MaterialTheme.colorScheme.error
            )
        )
    }
}
