// (nuevo) Configuración del servidor Node.js propio.
package pe.rejuega.app.data.remote

object ApiConfig {
    // (modificado) Usa la IP local de tu PC para celular físico.
    // Reemplaza 192.168.1.100 por TU IP real (paso 1).
    const val NODE_BASE_URL = "http://192.168.100.106:3000/"

    // (nuevo) Alias para emulador (por si quieres cambiar rápidamente).
    const val EMULATOR_BASE_URL = "http://10.0.2.2:3000/"
}
