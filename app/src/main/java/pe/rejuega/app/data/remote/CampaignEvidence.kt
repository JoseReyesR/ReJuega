// (nuevo) Documento de la colección "campaignGallery" en Firestore.
package pe.rejuega.app.data.remote

data class CampaignEvidence(
    val id: String = "",
    val campaignId: String = "",       // ID del documento de la campaña
    val campaignTitulo: String = "",
    val fotoUrl: String = "",
    val descripcion: String = "",
    val fechaSubida: Long = 0,
    val subidoPor: String = ""
)
