// (nuevo) Archivo: ImpactoDonacionDto.kt
// (nuevo) Descripción: DTO para el recurso impacto_donaciones de MockAPI.
// (nuevo) Se usa para enviar (POST) y recibir el registro del impacto acumulado del usuario.
package pe.rejuega.app.data.remote

import com.google.gson.annotations.SerializedName

// (nuevo) Data class que representa el impacto acumulado de un usuario.
data class ImpactoDonacionDto(

    // (nuevo) ID generado por MockAPI (opcional al enviar, obligatorio al recibir).
    @SerializedName("id")
    val id: String? = null,

    // (nuevo) UID del usuario de Firebase Authentication.
    @SerializedName("uid_usuario")
    val uidUsuario: String,

    // (nuevo) Total de kilos de CO2 ahorrados por todas sus donaciones.
    @SerializedName("co2_total_kg")
    val co2TotalKg: Double,

    // (nuevo) Fecha en formato texto (YYYY-MM-DD HH:mm:ss).
    @SerializedName("fecha_registro")
    val fechaRegistro: String
)