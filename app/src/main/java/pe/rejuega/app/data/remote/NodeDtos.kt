// (nuevo) DTOs que coinciden con el JSON del servidor Node (snake_case).
package pe.rejuega.app.data.remote

import com.google.gson.annotations.SerializedName

// (nuevo) Material tal como lo devuelve MySQL.
data class MaterialNodeDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("tipo_material") val tipoMaterial: String = "",
    @SerializedName("co2_ahorrado_kg") val co2AhorradoKg: Double = 0.0,
    @SerializedName("descripcion_impacto") val descripcionImpacto: String? = null,
    @SerializedName("imagen_url") val imagenUrl: String? = null
)

// (nuevo) Respuesta de lista: { success, count, data: [...] }
data class ApiListResponse<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("count") val count: Int = 0,
    @SerializedName("data") val data: List<T> = emptyList()
)

// (nuevo) Respuesta de objeto: { success, data: {...} } o { success, error }
data class ApiObjectResponse<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: T? = null,
    @SerializedName("error") val error: String? = null
)

// (nuevo) Respuesta de creación/error: { success, message, error, required }
data class ApiCreateResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("required") val required: List<String>? = null
)

// (nuevo) Cuerpo para crear un material.
data class MaterialCreateRequest(
    @SerializedName("tipo_material") val tipoMaterial: String,
    @SerializedName("co2_ahorrado_kg") val co2AhorradoKg: Double,
    @SerializedName("descripcion_impacto") val descripcionImpacto: String? = null,
    @SerializedName("imagen_url") val imagenUrl: String? = null
)

// (nuevo) Campaña tal como la devuelve MySQL (snake_case).
data class CampaignNodeDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("titulo") val titulo: String = "",
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("fecha_inicio") val fechaInicio: String = "",
    @SerializedName("fecha_fin") val fechaFin: String = "",
    @SerializedName("estado") val estado: String = "Activa",
    @SerializedName("imagen_url") val imagenUrl: String? = null,
    @SerializedName("creado_por") val creadoPor: String? = null,
    @SerializedName("fecha_creacion") val fechaCreacion: String? = null
)

// (nuevo) Usuario tal como lo devuelve MySQL.
data class UserNodeDto(
    @SerializedName("firebase_uid") val firebaseUid: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("correo") val correo: String = "",
    @SerializedName("rol") val rol: String = "Donante",
    @SerializedName("direccion") val direccion: String? = null,
    @SerializedName("telefono") val telefono: String? = null,
    @SerializedName("ciudad") val ciudad: String? = null,
    @SerializedName("genero") val genero: String? = null,
    @SerializedName("fcm_token") val fcmToken: String? = null
)

// (nuevo) Donación tal como la devuelve MySQL.
data class DonationNodeDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("user_id") val userId: String = "",
    @SerializedName("campaign_id") val campaignId: String = "",
    @SerializedName("nombre_juguete") val nombreJuguete: String = "",
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("condicion") val condicion: String = "Buen Estado",
    @SerializedName("foto_url") val fotoUrl: String = "",
    @SerializedName("estado_trazabilidad") val estadoTrazabilidad: String = "Pendiente",
    @SerializedName("motivo_rechazo") val motivoRechazo: String? = null,
    @SerializedName("material_tipo") val materialTipo: String? = null,
    @SerializedName("co2_ahorrado_kg") val co2AhorradoKg: Double = 0.0,
    @SerializedName("fecha_registro") val fechaRegistro: String? = null
)

// (nuevo) Punto de entrega.
data class DeliveryPointNodeDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("direccion") val direccion: String = "",
    @SerializedName("horario") val horario: String? = null,
    @SerializedName("dias_atencion") val diasAtencion: String? = null,
    @SerializedName("hora_inicio") val horaInicio: String? = null,
    @SerializedName("hora_fin") val horaFin: String? = null,
    @SerializedName("latitud") val latitud: Double = 0.0,
    @SerializedName("longitud") val longitud: Double = 0.0,
    @SerializedName("activo") val activo: Boolean = true,
    @SerializedName("creado_por") val creadoPor: String? = null
)

// (nuevo) Evento de recojo.
data class PickupEventNodeDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("campaign_id") val campaignId: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("lugar") val lugar: String? = null,
    @SerializedName("direccion") val direccion: String? = null,
    @SerializedName("fecha") val fecha: String = "",
    @SerializedName("hora") val hora: String = "",
    @SerializedName("latitud") val latitud: Double = 0.0,
    @SerializedName("longitud") val longitud: Double = 0.0,
    @SerializedName("activo") val activo: Boolean = true,
    @SerializedName("creado_por") val creadoPor: String? = null
)

// (nuevo) Evidencia de campaña.
data class CampaignEvidenceNodeDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("campaign_id") val campaignId: String = "",
    @SerializedName("foto_url") val fotoUrl: String = "",
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("subido_por") val subidoPor: String? = null,
    @SerializedName("fecha_subida") val fechaSubida: String? = null
)
