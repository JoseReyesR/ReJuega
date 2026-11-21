// (nuevo) Contrato REST contra el servidor Node.js propio.
package pe.rejuega.app.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE

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

    // (nuevo) Endpoints de campañas.
    @GET("api/campaigns")
    suspend fun obtenerCampaigns(): Response<ApiListResponse<CampaignNodeDto>>

    @GET("api/campaigns/{id}")
    suspend fun obtenerCampaignPorId(@Path("id") id: String): Response<ApiObjectResponse<CampaignNodeDto>>

    @POST("api/campaigns")
    suspend fun crearCampaign(@Body body: CampaignNodeDto): Response<ApiCreateResponse>

    @PUT("api/campaigns/{id}")
    suspend fun actualizarCampaign(@Path("id") id: String, @Body body: CampaignNodeDto): Response<ApiCreateResponse>

    @PUT("api/campaigns/{id}/estado")
    suspend fun cambiarEstadoCampaign(@Path("id") id: String, @Body body: Map<String, String>): Response<ApiCreateResponse>

    // (nuevo) DELETE /api/campaigns/:id
    @DELETE("api/campaigns/{id}")
    suspend fun eliminarCampaign(@Path("id") id: String): Response<ApiCreateResponse>

    // (nuevo) Endpoints de usuarios.
    @GET("api/users")
    suspend fun obtenerUsuarios(): Response<ApiListResponse<UserNodeDto>>

    @GET("api/users/{uid}")
    suspend fun obtenerUsuarioPorUid(@Path("uid") uid: String): Response<ApiObjectResponse<UserNodeDto>>

    @POST("api/users/sync")
    suspend fun sincronizarUsuario(@Body body: UserNodeDto): Response<ApiCreateResponse>

    @PUT("api/users/{uid}")
    suspend fun actualizarUsuario(@Path("uid") uid: String, @Body body: UserNodeDto): Response<ApiCreateResponse>
}
