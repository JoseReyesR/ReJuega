// (nuevo) Modelos de puntos de recojo en Firestore (HU-04, HU-14).
package pe.rejuega.app.data.remote

// (nuevo) Punto fijo de la ONG (siempre disponible).
data class PuntoEntregaFirestore(
    val id: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val horario: String = "",          // (modificado) Texto para mostrar
    val dias: List<String> = emptyList(),  // (nuevo) ["Lun","Mar",...]
    val horaInicio: String = "",       // (nuevo)
    val horaFin: String = "",          // (nuevo)
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val activo: Boolean = true,
    val creadoPor: String = ""
)

// (nuevo) Evento de recojo masivo asociado a una campaña.
// (nuevo) campaignId es el ID de documento Firestore de la campaña (estable).
data class PickupEventFirestore(
    val id: String = "",
    val campaignId: String = "",
    val campaignTitulo: String = "",
    val nombre: String = "",
    val lugar: String = "",
    val direccion: String = "",
    val fecha: String = "",       // YYYY-MM-DD
    val hora: String = "",        // HH:mm
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val activo: Boolean = true,
    val creadoPor: String = ""
)
