// (nuevo) Geocodificación gratuita con Nominatim (OpenStreetMap).
package pe.rejuega.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

// (nuevo) Resultado de búsqueda de direcciones.
data class NominatimResult(
    val place_id: Long = 0,
    val display_name: String = "",
    val lat: String = "",
    val lon: String = ""
)

interface NominatimService {
    // (nuevo) Busca direcciones y devuelve candidatas con coordenadas.
    @GET("search")
    suspend fun buscarDirecciones(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5,
        @Query("countrycodes") country: String = "pe"
    ): List<NominatimResult>
}
