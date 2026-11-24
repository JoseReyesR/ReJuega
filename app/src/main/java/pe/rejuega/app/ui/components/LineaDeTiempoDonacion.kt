// (nuevo) Línea de tiempo con la fecha de cada cambio de estado (HU-07 criterio 4).
package pe.rejuega.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.rejuega.app.data.remote.CambioEstado
import pe.rejuega.app.utils.EstadosTrazabilidad
import pe.rejuega.app.utils.formatarFechaHora

@Composable
fun LineaDeTiempoDonacion(
    historial: List<CambioEstado>,
    estadoActual: String
) {
    val indiceActual = EstadosTrazabilidad.indiceDe(estadoActual)

    Column(Modifier.fillMaxWidth()) {
        EstadosTrazabilidad.listaEstados.forEachIndexed { indice, nombre ->
            val completado = indice <= indiceActual
            // (nuevo) Fecha del hito si existe en el historial.
            val cambio = historial.firstOrNull { it.estado == nombre }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(24.dp).clip(CircleShape).background(
                        if (completado) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    if (completado) {
                        Icon(Icons.Default.Check, null, Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = nombre.replace("En Revision", "En revisión"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (indice == indiceActual) FontWeight.Bold else FontWeight.Normal
                    )
                    // (nuevo) Muestra la fecha del cambio cuando existe.
                    if (cambio != null) {
                        Text(
                            text = formatarFechaHora(cambio.fecha),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (indice < EstadosTrazabilidad.listaEstados.lastIndex) {
                Box(Modifier.padding(start = 11.dp, top = 2.dp, bottom = 2.dp)
                    .width(2.dp).height(12.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant))
            }
        }

        // (nuevo) Si fue rechazada, se muestra el hito rojo con su fecha.
        if (estadoActual == "Rechazado") {
            val rechazo = historial.firstOrNull { it.estado == "Rechazado" }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Close, null, Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Rechazada", color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    if (rechazo != null) {
                        Text(formatarFechaHora(rechazo.fecha),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}