// (modificado) Archivo: Routes.kt
// (modificado) Descripción: Rutas de navegación de la app ReJuega.
// (modificado) Cambio: Se agrega parámetro campaignId a la ruta de registro de juguetes.
package pe.rejuega.app.ui.navigation

// (modificado) Objeto central con todas las rutas de la aplicación.
object Routes {

    // (modificado) Pantalla de Login. Se usará en Sprint 5 con Firebase Auth.
    const val LOGIN = "login"

    // (modificado) Pantalla Home: lista de campañas.
    const val HOME = "home"

    // (modificado) Detalle de campaña. Recibe el ID local y el ID de Firestore.
    const val CAMPAIGN_DETAIL = "campaign_detail/{campaignId}/{firestoreId}"

    // (modificado) La ruta de registro ahora lleva también el título de la campaña y el firestoreId.
    const val REGISTER_TOY = "register_toy/{campaignId}/{campaignTitulo}/{campaignFirestoreId}"

    // (modificado) Pantalla de trazabilidad. Se implementará más adelante.
    const val TRACEABILITY = "traceability"

    // (modificado) Galería de evidencias. Se implementará más adelante.
    const val GALLERY = "gallery"

    // (modificado) Perfil del usuario. Se implementará más adelante.
    const val PROFILE = "profile"

    // (modificado) Panel de administración. Se implementará más adelante.
    const val ADMIN_PANEL = "admin_panel"

    // (modificado) Archivo: Routes.kt - dentro del objeto Routes
    // (nuevo) Pantalla del Panel de Administración (solo para rol Administrador).
    const val PANEL_ADMIN = "panel_admin"

    // (modificado) Impacto Ambiental = materiales (como antes).
    const val IMPACTO_AMBIENTAL = "impacto_ambiental"
    // (nuevo) Mi Huella Ecológica = cálculo real de tus donaciones.
    const val MI_HUELLA_ECOLOGICA = "mi_huella_ecologica"

    // (modificado) Archivo: Routes.kt - dentro del objeto Routes
    // (nuevo) Pantalla de Perfil e Impacto Acumulado (HU-12).
    const val PERFIL = "perfil"

    // (modificado) Nuevas rutas para gestión de campañas.
    const val CAMPAIGN_ADMIN = "campaign_admin"
    const val CAMPAIGN_FORM = "campaign_form/{campaignId}"


    // (nuevo) Rutas para validación de donaciones.
    const val DONATION_ADMIN = "donation_admin"
    const val DONATION_DETAIL_ADMIN = "donation_detail_admin/{donacionUid}"
    const val NOTIFICATIONS = "notifications"
    // (nuevo) Ruta del mapa de puntos de recojo (HU-10).
    const val MAPA_PUNTOS_RECOJO = "mapa_puntos_recojo"
    // (nuevo) Ruta de pruebas del servidor.
    const val SERVIDOR_TEST = "servidor_test"

    // (nuevo) Rutas de gestión de puntos de recojo (HU-04, HU-14).
    const val PUNTOS_RECOJO_ADMIN = "puntos_recojo_admin"
    const val PUNTO_ENTREGA_FORM = "punto_entrega_form/{puntoId}"
    const val PICKUP_EVENT_FORM = "pickup_event_form/{eventId}"
    const val CAMPAIGN_GALLERY = "campaign_gallery/{campaignId}/{campaignTitulo}"

    fun puntoEntregaForm(puntoId: String) = "punto_entrega_form/$puntoId"
    fun pickupEventForm(eventId: String) = "pickup_event_form/$eventId"

    fun campaignForm(campaignId: String): String = "campaign_form/$campaignId"

    fun campaignGallery(campaignId: String, campaignTitulo: String): String =
        "campaign_gallery/$campaignId/${android.net.Uri.encode(campaignTitulo)}"


    // (modificado) Función auxiliar para construir la ruta de detalle de campaña.
    fun campaignDetail(campaignId: Long, firestoreId: String): String {
        val idValido = firestoreId.ifBlank { "none" }
        return "campaign_detail/$campaignId/$idValido"
    }

    // (modificado) Se codifica el título y firestoreId para que la donación se vincule bien en MySQL.
    fun registerToy(campaignId: Long, campaignTitulo: String, campaignFirestoreId: String): String =
        "register_toy/$campaignId/${android.net.Uri.encode(campaignTitulo)}/${android.net.Uri.encode(campaignFirestoreId)}"

    fun donationDetailAdmin(donacionUid: String): String = "donation_detail_admin/$donacionUid"

    const val EDITAR_DONACION = "editar_donacion/{donationId}/{campaignId}/{campaignTitulo}/{fotoUrl}"
    fun editarDonacion(donationId: String, campaignId: Long, campaignTitulo: String, fotoUrl: String): String =
        "editar_donacion/$donationId/$campaignId/${android.net.Uri.encode(campaignTitulo)}/${android.net.Uri.encode(fotoUrl)}"
}