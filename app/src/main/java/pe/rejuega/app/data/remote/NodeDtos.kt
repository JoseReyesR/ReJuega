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
