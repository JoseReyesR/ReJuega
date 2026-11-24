// (nuevo) Archivo: EstadosTrazabilidad.kt
// (nuevo) Descripción: Catálogo central de estados del proceso de donación.
// (nuevo) Implementa RB-02: los estados avanzan de forma secuencial, nunca se saltan pasos.
package pe.rejuega.app.utils

// (nuevo) Objeto singleton: se accede desde cualquier capa sin crear instancias.
object EstadosTrazabilidad {

    // (nuevo) Orden oficial y único del flujo de trazabilidad.
    // (nuevo) IMPORTANTE: se escriben sin tildes para evitar caracteres especiales en BD.
    val listaEstados = listOf(
        "Pendiente",     // (nuevo) Paso 0: El donante registró el juguete.
        "En Revision",   // (nuevo) Paso 1: La ONG está validando estado y fotos.
        "Aprobado",      // (nuevo) Paso 2: La ONG aprobó y asignó punto de entrega.
        "Entregado"      // (nuevo) Paso 3: El juguete fue recibido (estado final).
    )

    // (nuevo) Devuelve la posición del estado actual dentro del flujo.
    // (nuevo) Si llega un valor desconocido, devuelve 0 para no romper la UI.
    fun indiceDe(estado: String): Int {
        val posicion = listaEstados.indexOf(estado)
        return if (posicion < 0) 0 else posicion
    }

    // (nuevo) Devuelve el siguiente estado permitido.
    // (nuevo) Si ya estamos en "Entregado", devuelve null (no se puede avanzar más).
    fun siguienteDe(estado: String): String? {
        val indiceActual = indiceDe(estado)
        return if (indiceActual < listaEstados.lastIndex) {
            listaEstados[indiceActual + 1]
        } else {
            null
        }
    }

    // (nuevo) Indica si un juguete ya terminó su ciclo de donación.
    fun estaFinalizado(estado: String): Boolean {
        return estado == "Entregado"
    }
}