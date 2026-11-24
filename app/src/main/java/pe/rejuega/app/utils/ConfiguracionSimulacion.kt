// (nuevo) Archivo: ConfiguracionSimulacion.kt
// (nuevo) Descripción: Banderas de simulación exclusivamente para desarrollo académico.
// (nuevo) ADVERTENCIA: este archivo NO debe existir en una versión de producción.
package pe.rejuega.app.utils

// (nuevo) Objeto con configuraciones temporales del entorno de desarrollo.
object ConfiguracionSimulacion {

    // (nuevo) Simula que el usuario actual tiene rol Administrador.
    // (nuevo) Se usa solo para probar la RB-03 antes de integrar Firebase Authentication.
    // (nuevo) En Sprint 5 se reemplaza por: currentUser.isAdminFromFirestoreClaims()
    const val ES_ADMIN_SIMULADO = true

    // (nuevo) Cambia este valor a false para verificar que el botón desaparece de la UI.
    // (nuevo) Prueba recomendada antes de entregar el informe.
}