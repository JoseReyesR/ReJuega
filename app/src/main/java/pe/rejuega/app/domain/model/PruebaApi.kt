// (nuevo) Resultado de una prueba HTTP para mostrar en pantalla.
package pe.rejuega.app.domain.model

data class PruebaApi(
    val nombre: String,
    val metodo: String,
    val url: String,
    val codigo: Int,          // 200, 201, 400, 404, 500 o 0 (sin conexión)
    val exito: Boolean,
    val detalle: String,
    val fecha: Long = System.currentTimeMillis()
)
