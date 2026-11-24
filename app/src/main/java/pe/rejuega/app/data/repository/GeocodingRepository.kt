// (nuevo) Repository de geocodificación.
package pe.rejuega.app.data.repository

import pe.rejuega.app.data.remote.NominatimResult
import pe.rejuega.app.data.remote.RetrofitClient

class GeocodingRepository {
    private val service = RetrofitClient.getNominatimService()

    suspend fun buscar(query: String): List<NominatimResult> {
        return service.buscarDirecciones(query)
    }
}
