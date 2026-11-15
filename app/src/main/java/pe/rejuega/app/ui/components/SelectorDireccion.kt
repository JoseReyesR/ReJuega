// (nuevo) Campo de dirección + búsqueda + lista de sugerencias con coordenadas.
package pe.rejuega.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pe.rejuega.app.data.remote.NominatimResult

@Composable
fun SelectorDireccion(
    direccion: String,
    onDireccionChange: (String) -> Unit,
    buscando: Boolean,
    sugerencias: List<NominatimResult>,
    onBuscar: () -> Unit,
    onSeleccionar: (NominatimResult) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = direccion,
            onValueChange = onDireccionChange,
            label = { Text("Dirección (ej. Av. Perú 655)") },
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onBuscar) {
            if (buscando) CircularProgressIndicator(Modifier.width(20.dp).height(20.dp))
            else Icon(Icons.Default.Search, contentDescription = "Buscar coordenadas")
        }
    }

    // (nuevo) Lista de candidatas: al tocar una, se llenan las coordenadas.
    if (sugerencias.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        Column {
            sugerencias.forEach { resultado ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clickable { onSeleccionar(resultado) }
                ) {
                    Text(
                        text = resultado.display_name,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}
