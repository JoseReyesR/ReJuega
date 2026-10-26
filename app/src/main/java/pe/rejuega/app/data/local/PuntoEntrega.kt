// Archivo: PuntoEntrega.kt
// Descripción: Entidad para los lugares físicos donde se reciben juguetes.

package pe.rejuega.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puntos_entrega")
data class PuntoEntrega(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val direccion: String,
    val horario: String,
    val latitud: Double,
    val longitud: Double,
    val activo: Boolean = true // (modificado) Permite desactivar puntos sin borrarlos (baja lógica)
)