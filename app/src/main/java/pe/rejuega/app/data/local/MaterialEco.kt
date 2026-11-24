//  Archivo: MaterialEco.kt
//  Descripción: Entidad para cachear los datos de impacto ambiental (ODS 12).

package pe.rejuega.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materiales_eco")
data class MaterialEco(
    @PrimaryKey
    val id: String, // (modificado) ID de texto, ej: "plastico_abs"
    val tipoMaterial: String,
    val co2AhorradoKg: Double,
    val descripcionImpacto: String
)