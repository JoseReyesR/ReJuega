// (modificado) Menú lateral v2: encabezado colorido, secciones por rol y cierre vistoso.
package pe.rejuega.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class DestinoMenu(val id: String, val nombre: String, val icono: ImageVector)

@Composable
private fun SectionTitle(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun MenuLateralReJuega(
    nombreUsuario: String,
    rolActual: String = "Donante",
    notificacionesNoLeidas: Int,
    onSeleccion: (String) -> Unit
) {
    val esGestor = rolActual == "Gestor"
    val esAdmin = rolActual == "Administrador"
    val esGestorOAdmin = esGestor || esAdmin
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {

        // (nuevo) Encabezado colorido con logo, rol y usuario.
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Toys, null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "ReJuega",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        rolActual,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                nombreUsuario.ifBlank { "Usuario" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // (nuevo) Secciones según rol: Admin y Gestor ven gestión de la ONG.
        if (esGestorOAdmin) {
            SectionTitle("GESTIÓN DE LA ONG")
            buildList {
                add(DestinoMenu("panel", "Panel de Administración", Icons.Default.AdminPanelSettings))
                add(DestinoMenu("campaigns", "Gestión de Campañas", Icons.Default.Campaign))
                add(DestinoMenu("donations", "Revisión de Donaciones", Icons.Default.CheckCircle))
                add(DestinoMenu("puntos", "Gestión de Puntos de Recojo", Icons.Default.Place))
                if (esAdmin) {
                    add(DestinoMenu("usuarios", "Gestión de Usuarios", Icons.Default.People))
                }
                add(DestinoMenu("servidor", "Pruebas del Servidor", Icons.Default.Dns))
            }.forEach { d ->
                NavigationDrawerItem(
                    label = { Text(d.nombre) },
                    icon = { Icon(d.icono, null, tint = MaterialTheme.colorScheme.primary) },
                    selected = false,
                    onClick = { onSeleccion(d.id) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
            SectionTitle("HERRAMIENTAS")
            NavigationDrawerItem(
                label = { Text("Mapa de Puntos de Recojo") },
                icon = { Icon(Icons.Default.Map, null, tint = MaterialTheme.colorScheme.secondary) },
                selected = false,
                onClick = { onSeleccion("mapa") },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        } else {
            SectionTitle("MI ESPACIO")
            listOf(
                DestinoMenu("trazabilidad", "Mis Donaciones", Icons.Default.History),
                DestinoMenu("impacto", "Impacto Ambiental", Icons.Default.Eco),
                DestinoMenu("huella", "Mi Huella Ecológica", Icons.Default.Eco),
                DestinoMenu("perfil", "Mi Perfil", Icons.Default.Person)
            ).forEach { d ->
                NavigationDrawerItem(
                    label = { Text(d.nombre) },
                    icon = { Icon(d.icono, null, tint = MaterialTheme.colorScheme.primary) },
                    selected = false,
                    onClick = { onSeleccion(d.id) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
            NavigationDrawerItem(
                label = {
                    Text(
                        if (notificacionesNoLeidas > 0) "Notificaciones ($notificacionesNoLeidas)"
                        else "Notificaciones"
                    )
                },
                icon = { Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.secondary) },
                selected = false,
                onClick = { onSeleccion("notificaciones") },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            SectionTitle("HERRAMIENTAS")
            NavigationDrawerItem(
                label = { Text("Mapa de Puntos de Recojo") },
                icon = { Icon(Icons.Default.Map, null, tint = MaterialTheme.colorScheme.secondary) },
                selected = false,
                onClick = { onSeleccion("mapa") },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        HorizontalDivider()

        // (nuevo) Cierre de sesión vistoso en rojo.
        NavigationDrawerItem(
            label = {
                Text(
                    "Cerrar sesión",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            },
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp, null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            selected = false,
            onClick = { onSeleccion("logout") },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
