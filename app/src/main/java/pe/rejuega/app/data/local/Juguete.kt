// Archivo: Juguete.kt
// Descripción: Entidad que representa la propuesta de donación de un usuario.

package pe.rejuega.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "juguetes")
data class Juguete(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val campaignId: Long, // (modificado) Relación con la entidad Campaign (por la ñ)
    val nombre: String,
    val descripcion: String,
    val estado: String,     // (modificado) Regla de negocio: "Nuevo" o "Buen Estado"
    val fotoUrl: String,
    val estadoTrazabilidad: String = "Pendiente" // (modificado) Estados: Pendiente, En Revision, Aprobado, Entregado
)