// (nuevo) Archivo: MaterialJugueteDto.kt
// (nuevo) Descripción: Data Transfer Object (DTO) para el recurso materiales_juguetes de MockAPI.
// (nuevo) Motivo: Separamos el modelo de la API (DTO) del modelo local (MaterialEco) para mayor flexibilidad.
package pe.rejuega.app.data.remote

import com.google.gson.annotations.SerializedName

// (nuevo) Data class que representa un material de juguete según la API REST.
// (nuevo) @SerializedName mapea los campos del JSON a las propiedades de Kotlin.
data class MaterialJugueteDto(

    // (nuevo) ID del material (viene como String en MockAPI, aunque sea numérico).
    @SerializedName("id")
    val id: String,

    // (nuevo) Tipo de material: "Plástico ABS", "Madera y textiles", etc.
    @SerializedName("tipoMaterial")
    val tipoMaterial: String,

    // (nuevo) Kilos de CO₂ ahorrados al reutilizar este tipo de juguete.
    @SerializedName("co2AhorradoKg")
    val co2AhorradoKg: Double,

    // (nuevo) Descripción del impacto ambiental positivo.
    @SerializedName("descripcionImpacto")
    val descripcionImpacto: String,

    // (nuevo) URL de imagen ilustrativa (opcional, puede venir vacía).
    @SerializedName("imagenUrl")
    val imagenUrl: String?
)