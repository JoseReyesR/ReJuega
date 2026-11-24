// (nuevo) Archivo: Usuario.kt
// (nuevo) Descripción: Modelo de usuario para Firebase Authentication y Firestore.
// (nuevo) El nombre se mantiene en español porque no contiene caracteres especiales.
package pe.rejuega.app.domain.model

// (nuevo) Data class que representa el documento de usuario en Firestore.
// (nuevo) Firestore necesita una clase con constructor vacío o valores por defecto.
data class Usuario(
    val uid: String = "",
    val correo: String = "",
    val nombre: String = "",
    val rol: String = "Donante",
    val fechaRegistro: String = "",
    val direccion: String = "",   // (nuevo) referencia para coordinación de recojo
    val telefono: String = "",    // (nuevo) contacto para la ONG
    val ciudad: String = "",      // (nuevo) logística de campañas
    val genero: String = ""       // (nuevo) estadísticas de la ONG
) {
    fun esAdministrador(): Boolean = rol == "Administrador"
}