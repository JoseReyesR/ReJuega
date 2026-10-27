// (nuevo) Archivo: MaterialJugueteService.kt
// (nuevo) Descripción: Interfaz de servicio Retrofit para el recurso materiales_juguetes.
// (nuevo) Define las operaciones HTTP (GET, POST) que la app puede realizar contra MockAPI.
package pe.rejuega.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

// (nuevo) Import necesario para enviar datos en el cuerpo de la petición HTTP.
import retrofit2.http.Body
import retrofit2.http.POST

// (nuevo) Interfaz que Retrofit usa para generar automáticamente el cliente HTTP.
interface MaterialJugueteService {

    // (nuevo) Obtiene la lista completa de materiales de juguetes desde MockAPI.
    // (nuevo) @GET define que es una petición HTTP GET.
    // (nuevo) "materiales_juguetes" es la ruta relativa que se concatena a la URL base.
    // (nuevo) Retorna una lista de MaterialJugueteDto envuelta en Response de Retrofit.
    @GET("materiales_juguetes")
    suspend fun obtenerMateriales(): List<MaterialJugueteDto>

    // (nuevo) Obtiene un material específico por su ID.
    // (nuevo) @Path reemplaza {id} con el valor real pasado como parámetro.
    // (nuevo) Útil para mostrar el detalle de impacto de un juguete específico.
    @GET("materiales_juguetes/{id}")
    suspend fun obtenerMaterialPorId(@Path("id") id: String): MaterialJugueteDto

    // (nuevo) Registra el impacto ecológico acumulado del usuario en MockAPI.
    // (nuevo) @POST define que es una petición HTTP POST.
    // (nuevo) @Body indica que el objeto ImpactoDonacionDto se enviará como JSON en el cuerpo.
    @POST("impacto_donaciones")
    suspend fun registrarImpacto(@Body impacto: ImpactoDonacionDto): ImpactoDonacionDto
}