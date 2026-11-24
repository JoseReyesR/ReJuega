// (nuevo) Documento de la colección "notifications" en Firestore.
package pe.rejuega.app.data.remote

data class Notificacion(
    val id: String = "",
    val uidDestinatario: String = "",     // UID del donante que la recibe
    val titulo: String = "",              // Ej. "Tu donación fue aprobada"
    val mensaje: String = "",             // Ej. "El juguete 'Carrito' pasó a Aprobado."
    val donacionId: String = "",          // Relacionada a qué donación
    val tipo: String = "estado",          // tipo: estado / rechazo / general
    val leida: Boolean = false,
    val fechaCreacion: Long = 0
)