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

// (nuevo) Formatea millis a fecha y hora legible en zona local.
fun formatarFechaHora(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}

// (nuevo) Convierte ISO 8601 o YYYY-MM-DD a formato visible dd/MM/yyyy.
fun isoAFechaVisible(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val soloFecha = iso.substring(0, 10)
        val partes = soloFecha.split("-")
        if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else soloFecha
    } catch (e: Exception) {
        iso
    }
}

// (nuevo) Convierte dd/MM/yyyy a YYYY-MM-DD para MySQL.
fun fechaVisibleAIso(visible: String?): String {
    if (visible.isNullOrBlank()) return ""
    return try {
        val partes = visible.split("/")
        if (partes.size == 3) "${partes[2]}-${partes[1]}-${partes[0]}" else visible
    } catch (e: Exception) {
        visible
    }
}