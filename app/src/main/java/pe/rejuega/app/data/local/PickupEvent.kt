// (nuevo) Archivo: PickupEvent.kt
// (nuevo) Descripción: Entidad Room que representa un evento de recojo masivo por campaña.
// (nuevo) Corresponde a la colección "pickupEvents" de Firestore.
// (nuevo) Implementa HU-14: punto de recojo masivo asociado a una campaña.
package pe.rejuega.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// (nuevo) Tabla local para cachear los eventos de recojo (RB-05: funcionamiento offline).
@Entity(tableName = "pickup_events")
data class PickupEvent(
    // (nuevo) ID local autogenerado. En Firestore tendrá su propio ID de documento.
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // (nuevo) ID de la campaña a la que pertenece este evento de recojo.
    // (nuevo) Relación 1 a N: una campaña puede tener varios eventos de recojo.
    val campaignId: Long,

    // (nuevo) Nombre descriptivo del evento de recojo.
    val nombre: String,

    // (nuevo) Lugar del evento (ej. "Parque Central de Trujillo").
    val lugar: String,

    // (nuevo) Dirección completa del evento.
    val direccion: String,

    // (nuevo) Fecha del evento en formato YYYY-MM-DD.
    val fecha: String,

    // (nuevo) Hora del evento en formato HH:mm.
    val hora: String,

    // (nuevo) Coordenadas geográficas para mostrar en el mapa (futuro).
    val latitud: Double,
    val longitud: Double,

    // (nuevo) Baja lógica: si es false, el evento no se muestra en el mapa.
    val activo: Boolean = true
)