// (nuevo) Archivo: TrazabilidadTimeline.kt
// (nuevo) Descripción: Componente reutilizable de línea de tiempo para donaciones.
// (nuevo) Muestra los 4 pasos y resalta los ya alcanzados según el estado actual.
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pe.rejuega.app.utils.EstadosTrazabilidad

// (nuevo) Recibe el estado actual y dibuja visualmente el progreso.
@Composable
fun TrazabilidadTimeline(estadoActual: String) {

    // (nuevo) Calculamos hasta qué paso ya llegó esta donación.
    val indiceActual = EstadosTrazabilidad.indiceDe(estadoActual)

    // (nuevo) Columna con un paso por fila, estilo "línea de tiempo vertical".
    Column(modifier = Modifier.fillMaxWidth()) {

        EstadosTrazabilidad.listaEstados.forEachIndexed { indice, nombreEstado ->

            // (nuevo) Un paso se considera completado si su índice ya fue alcanzado.
            val estaCompletado = indice <= indiceActual

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // (nuevo) Círculo del paso: verde si está completado, gris si falta.
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (estaCompletado) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // (nuevo) Mostrar check solo en los pasos completados.
                    if (estaCompletado) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Paso completado: $nombreEstado",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // (nuevo) Etiqueta legible del paso (sin guiones ni caracteres raros).
                Text(
                    text = nombreEstado.replace("En Revision", "En revisión"),
                    style = MaterialTheme.typography.bodyMedium,
                    // (nuevo) El paso actual se resalta en negrita.
                    color = if (estaCompletado) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // (nuevo) Separador vertical suave entre pasos (excepto el último).
            if (indice < EstadosTrazabilidad.listaEstados.lastIndex) {
                Box(
                    modifier = Modifier
                        .padding(start = 11.dp, top = 2.dp, bottom = 2.dp)
                        .width(2.dp)
                        .height(12.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
}