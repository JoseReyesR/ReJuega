// (nuevo) Archivo: Routes.kt
// (nuevo) Descripción: Definición centralizada de las rutas de navegación de la app.
// (nuevo) Cada constante representa una pantalla. Se usarán en el NavHost.
// (nuevo) Corresponde a la arquitectura MVVM: La navegación es parte de la capa UI.
package pe.rejuega.app.ui.navigation

// (nuevo) Objeto que agrupa todas las rutas de la app como constantes de texto.
// (nuevo) Usar constantes evita errores tipográficos y facilita el mantenimiento.
object Routes {
    // (nuevo) Pantalla de Login (Sprint 5 - Firebase Auth)
    const val LOGIN = "login"

    // (nuevo) Pantalla Home: Lista de campañas activas (Sprint 2)
    const val HOME = "home"

    // (nuevo) Pantalla Detalle de Campaña: Información completa de una campaña (Sprint 2)
    // (nuevo) El parámetro {campaignId} se reemplazará con el ID real de la campaña al navegar.
    const val CAMPAIGN_DETAIL = "campaign_detail/{campaignId}"

    // (nuevo) Pantalla de Registro de Juguete: Formulario para proponer una donación (Sprint 2)
    const val REGISTER_TOY = "register_toy"

    // (nuevo) Pantalla de Trazabilidad: Historial de estados de mis donaciones (Sprint 5)
    const val TRACEABILITY = "traceability"

    // (nuevo) Pantalla de Galería: Evidencias fotográficas de entregas (Sprint 5)
    const val GALLERY = "gallery"

    // (nuevo) Pantalla de Perfil: Datos del usuario y huella ecológica acumulada (Sprint 4)
    const val PROFILE = "profile"

    // (nuevo) Pantalla de Panel de Administración: Solo visible para rol Administrador (Sprint 5)
    const val ADMIN_PANEL = "admin_panel"

    // (nuevo) Función auxiliar para construir la ruta de detalle con el ID real.
    // (nuevo) Ejemplo: campaignDetail(5) retorna "campaign_detail/5"
    fun campaignDetail(campaignId: Long): String {
        return "campaign_detail/$campaignId"
    }
}