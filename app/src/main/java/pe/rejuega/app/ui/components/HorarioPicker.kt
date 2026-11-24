// (nuevo) Selector de horario: días como chips + horas inicio/fin desplegables.
package pe.rejuega.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import java.util.Locale

// (nuevo) Días y horas disponibles.
val DIAS_SEMANA = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
val HORAS_DISPONIBLES = buildList {
    for (h in 7..21) {
        add(String.format(Locale.US, "%02d:00", h))
        add(String.format(Locale.US, "%02d:30", h))
    }
}

// (nuevo) Construye el texto de horario para guardar/mostrar.
fun construirHorario(dias: List<String>, inicio: String, fin: String): String {
    val diasOrdenados = DIAS_SEMANA.filter { dias.contains(it) }
    return if (diasOrdenados.isEmpty() || inicio.isBlank() || fin.isBlank()) ""
    else diasOrdenados.joinToString(", ") + " de $inicio a $fin"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeDropdown(
    label: String,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            HORAS_DISPONIBLES.forEach { hora ->
                DropdownMenuItem(
                    text = { Text(hora) },
                    onClick = { onSelect(hora); expanded = false }
                )
            }
        }
    }
}

@Composable
fun HorarioPicker(
    diasSeleccionados: List<String>,
    onDiasChange: (List<String>) -> Unit,
    horaInicio: String,
    onHoraInicioChange: (String) -> Unit,
    horaFin: String,
    onHoraFinChange: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text("Días de atención")
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DIAS_SEMANA.forEach { dia ->
                FilterChip(
                    selected = diasSeleccionados.contains(dia),
                    onClick = {
                        onDiasChange(
                            if (diasSeleccionados.contains(dia)) diasSeleccionados - dia
                            else diasSeleccionados + dia
                        )
                    },
                    label = { Text(dia) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        TimeDropdown("Hora inicio", horaInicio, onHoraInicioChange)
        Spacer(Modifier.height(8.dp))
        TimeDropdown("Hora fin", horaFin, onHoraFinChange)
    }
}
