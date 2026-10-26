// (modificado) Archivo: Campaign.kt
// (modificado) Descripción: Entidad que representa una campaña de donación.
// (modificado) Motivo: Se usa el nombre "Campaign" ÚNICAMENTE porque la palabra "Campaña" contiene la letra 'ñ'.
package pe.rejuega.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// (modificado) El nombre de la tabla en la base de datos será "campaigns"
@Entity(tableName = "campaigns")
data class Campaign(
    // (modificado) ID autogenerado por Room
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // (modificado) Campos en español sin tildes para evitar caracteres especiales
    val titulo: String,
    val descripcion: String,
    val fechaInicio: String, // (modificado) Formato sugerido: YYYY-MM-DD
    val fechaFin: String,
    val estado: String,      // (modificado) Ejemplos: "Activa", "Cerrada"
    val imagenUrl: String
)