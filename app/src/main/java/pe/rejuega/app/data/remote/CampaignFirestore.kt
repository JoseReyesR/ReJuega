// (nuevo) Modelo de campaña para Firestore.
package pe.rejuega.app.data.remote

// (nuevo) Representa un documento de la colección "campaigns".
data class CampaignFirestore(
    val id: String = "",              // (nuevo) ID del documento Firestore
    val titulo: String = "",
    val descripcion: String = "",
    val fechaInicio: String = "",     // (nuevo) Formato YYYY-MM-DD
    val fechaFin: String = "",
    val estado: String = "Activa",    // (nuevo) "Activa" o "Cerrada"
    val imagenUrl: String = "",
    val creadoPor: String = "",       // (nuevo) UID del admin que la creó
    val fechaCreacion: Long = 0
)