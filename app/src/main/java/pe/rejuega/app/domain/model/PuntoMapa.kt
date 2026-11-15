// (nuevo) Punto unificado para el mapa: puede ser fijo o evento de recojo.
package pe.rejuega.app.domain.model

data class PuntoMapa(
    val id: String,
    val tipo: String,              // "Punto fijo" o "Evento de recojo"
    val nombre: String,
    val direccion: String,
    val detalle: String,           // horario (fijo) o fecha+hora (evento)
    val latitud: Double,
    val longitud: Double,
    val campaignTitulo: String = ""
)
