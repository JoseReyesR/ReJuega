// (nuevo) Archivo: utils/FechaUtils.kt
// (nuevo) Funciones auxiliares para convertir fechas entre formatos.
package pe.rejuega.app.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// (nuevo) Convierte milliseconds UTC a formato YYYY-MM-DD.
// (nuevo) El DatePicker de Material 3 retorna milliseconds en UTC.
fun convertirMillisAFecha(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(millis))
}

// (nuevo) Convierte una fecha YYYY-MM-DD a milliseconds UTC.
// (nuevo) Se usa para que el DatePicker abra en la fecha existente al editar.
fun convertirFechaAMillis(fecha: String): Long? {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        sdf.parse(fecha)?.time
    } catch (e: Exception) {
        null
    }
}