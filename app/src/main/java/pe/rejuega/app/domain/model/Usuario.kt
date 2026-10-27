// (nuevo) Archivo: Usuario.kt
// (nuevo) Descripción: Modelo de usuario para Firebase Authentication y Firestore.
// (nuevo) El nombre se mantiene en español porque no contiene caracteres especiales.
package pe.rejuega.app.domain.model

// (nuevo) Data class que representa el documento de usuario en Firestore.
// (nuevo) Firestore necesita una clase con constructor vacío o valores por defecto.
data class Usuario(

    // (nuevo) UID generado por Firebase Authentication.
    val uid: String = "",

    // (nuevo) Correo electrónico del usuario.
    val correo: String = "",

    // (nuevo) Nombre visible del usuario.
    val nombre: String = "",

    // (nuevo) Rol del usuario.
    // (nuevo) Valores permitidos: "Donante" o "Administrador".
    val rol: String = "Donante",

    // (nuevo) Fecha de registro en formato texto simple.
    val fechaRegistro: String = ""
) {

    // (nuevo) Función auxiliar para saber si el usuario es administrador.
    // (nuevo) Esta función será usada más adelante para mostrar u ocultar acciones.
    fun esAdministrador(): Boolean {
        return rol == "Administrador"
    }
}