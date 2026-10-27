// (nuevo) Archivo: AuthRepository.kt
// (nuevo) Descripción: Repositorio de autenticación con Firebase Auth y Firestore.
// (nuevo) Responsabilidad: separar la lógica de Firebase del ViewModel.
package pe.rejuega.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import pe.rejuega.app.domain.model.Usuario
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// (nuevo) Repositorio que gestiona autenticación y perfil de usuario.
class AuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    // (nuevo) Formato simple de fecha para guardar en Firestore.
    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // (nuevo) Obtiene el usuario actual desde Firebase Authentication y Firestore.
    // (nuevo) Si Firestore falla, devuelve un usuario básico con rol Donante.
    suspend fun obtenerUsuarioActual(): Usuario? {

        // (nuevo) Obtenemos el usuario autenticado.
        val firebaseUser = auth.currentUser ?: return null

        return try {
            // (nuevo) Buscamos el documento del usuario en la colección "users".
            val snapshot = firestore
                .collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            // (nuevo) Si el documento existe, lo convertimos a Usuario.
            if (snapshot.exists()) {
                snapshot.toObject(Usuario::class.java)
            } else {
                // (nuevo) Si no existe documento, devolvemos un usuario básico.
                Usuario(
                    uid = firebaseUser.uid,
                    correo = firebaseUser.email.orEmpty(),
                    nombre = firebaseUser.displayName.orEmpty(),
                    rol = "Donante",
                    fechaRegistro = formatoFecha.format(Date())
                )
            }
        } catch (e: Exception) {
            // (nuevo) Si Firestore falla por red o permisos,
            // (nuevo) igual devolvemos un usuario básico para no bloquear la app.
            Usuario(
                uid = firebaseUser.uid,
                correo = firebaseUser.email.orEmpty(),
                nombre = firebaseUser.displayName.orEmpty(),
                rol = "Donante",
                fechaRegistro = formatoFecha.format(Date())
            )
        }
    }

    // (nuevo) Registra un usuario nuevo en Firebase Authentication.
    // (nuevo) Luego crea su documento en Firestore con rol Donante.
    suspend fun registrarUsuario(
        nombre: String,
        correo: String,
        password: String
    ) {
        // (nuevo) Creamos el usuario en Firebase Authentication.
        val authResult = auth.createUserWithEmailAndPassword(correo, password).await()

        // (nuevo) Obtenemos el UID asignado por Firebase.
        val uid = authResult.user?.uid
            ?: throw Exception("No se pudo obtener el UID del usuario.")

        // (nuevo) Creamos el objeto Usuario que se guardará en Firestore.
        val usuario = Usuario(
            uid = uid,
            correo = correo,
            nombre = nombre,
            rol = "Donante",
            fechaRegistro = formatoFecha.format(Date())
        )

        // (nuevo) Guardamos el documento en la colección "users".
        firestore
            .collection("users")
            .document(uid)
            .set(usuario)
            .await()
    }

    // (nuevo) Inicia sesión con correo y contraseña.
    suspend fun iniciarSesion(correo: String, password: String) {
        auth.signInWithEmailAndPassword(correo, password).await()
    }

    // (nuevo) Cierra la sesión actual.
    // (nuevo) No es suspend porque signOut es una operación local rápida.
    fun cerrarSesion() {
        auth.signOut()
    }
}