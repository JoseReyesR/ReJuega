// (nuevo) Contrato REST contra el servidor Node.js propio.
package pe.rejuega.app.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST

interface NodeApiService {

    // (nuevo) GET 200: lista de materiales.
    @GET("api/materiales")
    suspend fun obtenerMateriales(): Response<ApiListResponse<MaterialNodeDto>>

    // (nuevo) GET 200 o 404: material por ID.
    @GET("api/materiales/{id}")
    suspend fun obtenerMaterialPorId(@Path("id") id: Int): Response<ApiObjectResponse<MaterialNodeDto>>

    // (nuevo) POST 201 o 400: crear material.
    @POST("api/materiales")
    suspend fun crearMaterial(@Body body: MaterialCreateRequest): Response<ApiCreateResponse>

    // (nuevo) GET 404 global: ruta inexistente para demostrar el manejo de errores.
    @GET("api/esto-no-existe")
    suspend fun rutaInexistente(): Response<ResponseBody>
}
