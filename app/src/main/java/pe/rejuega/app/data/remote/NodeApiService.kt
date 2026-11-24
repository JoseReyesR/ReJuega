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
import retrofit2.http.Query

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

    @PUT("api/users/{uid}/rol")
    suspend fun cambiarRol(
        @Path("uid") uid: String,
        @Body body: Map<String, String>
    ): Response<ApiCreateResponse>

    // (nuevo) Endpoints de donaciones.
    @GET("api/donations")
    suspend fun obtenerDonaciones(
        @Query("user_id") userId: String? = null,
        @Query("campaign_id") campaignId: String? = null
    ): Response<ApiListResponse<DonationNodeDto>>

    @GET("api/donations/{id}")
    suspend fun obtenerDonacionPorId(@Path("id") id: String): Response<ApiObjectResponse<DonationNodeDto>>

    @POST("api/donations")
    suspend fun crearDonacion(@Body body: DonationNodeDto): Response<ApiCreateResponse>

    @PUT("api/donations/{id}/estado")
    suspend fun cambiarEstadoDonacion(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<ApiCreateResponse>

    @PUT("api/donations/{id}")
    suspend fun actualizarDonacion(@Path("id") id: String, @Body body: DonationNodeDto): Response<ApiCreateResponse>

    @DELETE("api/donations/{id}")
    suspend fun eliminarDonacion(@Path("id") id: String): Response<ApiCreateResponse>

    // (nuevo) Endpoints de puntos y eventos.
    @GET("api/delivery-points")
    suspend fun obtenerPuntosEntrega(): Response<ApiListResponse<DeliveryPointNodeDto>>

    @POST("api/delivery-points")
    suspend fun crearPuntoEntrega(@Body body: DeliveryPointNodeDto): Response<ApiCreateResponse>

    @PUT("api/delivery-points/{id}")
    suspend fun actualizarPuntoEntrega(@Path("id") id: String, @Body body: DeliveryPointNodeDto): Response<ApiCreateResponse>

    @GET("api/pickup-events")
    suspend fun obtenerEventosRecojo(
        @Query("campaign_id") campaignId: String? = null
    ): Response<ApiListResponse<PickupEventNodeDto>>

    @POST("api/pickup-events")
    suspend fun crearEventoRecojo(@Body body: PickupEventNodeDto): Response<ApiCreateResponse>

    @PUT("api/pickup-events/{id}")
    suspend fun actualizarEventoRecojo(@Path("id") id: String, @Body body: PickupEventNodeDto): Response<ApiCreateResponse>

    // (nuevo) Endpoints de galería.
    @GET("api/campaign-gallery/campaign/{campaignId}")
    suspend fun obtenerEvidencias(@Path("campaignId") campaignId: String): Response<ApiListResponse<CampaignEvidenceNodeDto>>

    @POST("api/campaign-gallery")
    suspend fun crearEvidencia(@Body body: CampaignEvidenceNodeDto): Response<ApiCreateResponse>

    @DELETE("api/campaign-gallery/{id}")
    suspend fun eliminarEvidencia(@Path("id") id: String): Response<ApiCreateResponse>

    // (nuevo) Endpoints de notificaciones.
    @GET("api/notifications/user/{userId}")
    suspend fun obtenerNotificaciones(@Path("userId") userId: String): Response<ApiListResponse<NotificationNodeDto>>

    @POST("api/notifications")
    suspend fun crearNotificacion(@Body body: NotificationNodeDto): Response<ApiCreateResponse>

    @PUT("api/notifications/{id}/read")
    suspend fun marcarNotificacionLeida(@Path("id") id: String): Response<ApiCreateResponse>

    @PUT("api/notifications/user/{userId}/read-all")
    suspend fun marcarTodasLeidas(@Path("userId") userId: String): Response<ApiCreateResponse>

    @GET("api/notifications/user/{userId}/unread-count")
    suspend fun contarNoLeidas(@Path("userId") userId: String): Response<ApiObjectResponse<Map<String, Int>>>
}
