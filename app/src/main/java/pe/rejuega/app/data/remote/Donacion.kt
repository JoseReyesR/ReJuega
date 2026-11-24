// (nuevo) Archivo: Donacion.kt
// (nuevo) Modelos del documento de donación en Firestore.
// (nuevo) Antes vivían dentro de DonationRepository.kt; se extraen por orden.
package pe.rejuega.app.data.remote

// (modificado) Documento de la colección "donations" con todos los campos acumulados.
data class Donacion(
    val id: String = "",                 // ID del documento Firestore
    val uid: String = "",                // UID del donante dueño
    val campaignId: Long = 0,            // ID local de campaña (legado)
    val campaignFirestoreId: String = "",// (nuevo) String para Firestore/Node
    val campaignTitulo: String = "",     // Campaña elegida (desnormalizado)
    val nombre: String = "",
    val descripcion: String = "",
    val estado: String = "",             // Condición: Nuevo / Buen Estado
    val fotoUrl: String = "",
    val estadoTrazabilidad: String = "Pendiente",
    val motivoRechazo: String? = null,   // Solo si fue rechazada
    // (nuevo) Historial inmutable de cambios de estado (HU-07 criterio 4, RB-11).
    val historialEstados: List<CambioEstado> = emptyList(),
    val fechaRegistro: Long = 0,
    // (nuevo) Campos de material para la huella ecológica real.
    val materialTipo: String = "",
    val co2AhorradoKg: Double = 0.0
)

// (nuevo) Un hito de la trazabilidad: qué estado, cuándo y quién lo cambió.
data class CambioEstado(
    val estado: String = "",
    val fecha: Long = 0,
    val cambiadoPor: String = ""
)