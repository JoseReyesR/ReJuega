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

    // (modificado) Detalle de campaña. Recibe el ID de la campaña como parámetro.
    const val CAMPAIGN_DETAIL = "campaign_detail/{campaignId}"

    // (modificado) Registro de juguete. Ahora recibe el ID de la campaña seleccionada.
    const val REGISTER_TOY = "register_toy/{campaignId}"

    // (modificado) Pantalla de trazabilidad. Se implementará más adelante.
    const val TRACEABILITY = "traceability"

    // (modificado) Galería de evidencias. Se implementará más adelante.
    const val GALLERY = "gallery"

    // (modificado) Perfil del usuario. Se implementará más adelante.
    const val PROFILE = "profile"

    // (modificado) Panel de administración. Se implementará más adelante.
    const val ADMIN_PANEL = "admin_panel"

    // (nuevo) Pantalla de impacto ambiental (HU-11, ODS 12).
    const val IMPACTO_AMBIENTAL = "impacto_ambiental"

    // (modificado) Función auxiliar para construir la ruta de detalle de campaña.
    fun campaignDetail(campaignId: Long): String {
        return "campaign_detail/$campaignId"
    }

    // (nuevo) Función auxiliar para construir la ruta de registro de juguete.
    fun registerToy(campaignId: Long): String {
        return "register_toy/$campaignId"
    }
}