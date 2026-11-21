// (nuevo) Archivo: AuthRepository.kt
// (nuevo) Descripción: Repositorio de autenticación con Firebase Auth y Firestore.
// (nuevo) Responsabilidad: separar la lógica de Firebase del ViewModel.
package pe.rejuega.app.data.remote

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
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

        // (nuevo) Sincroniza a MySQL con el UID real que acabamos de obtener.
        try { sincronizarUsuarioActual() } catch (_: Exception) {}
    }

    // (nuevo) Inicia sesión con correo y contraseña.
    suspend fun iniciarSesion(correo: String, password: String) {
        auth.signInWithEmailAndPassword(correo, password).await()
    }


    // (modificado) Guarda token FCM: Node primario, Firestore respaldo.
    suspend fun guardarTokenFCM() {
        val uid = auth.currentUser?.uid ?: return
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            val userDto = UserNodeDto(firebaseUid = uid, nombre = "", correo = "", fcmToken = token)
            try { RetrofitClient.getNodeApiService().actualizarUsuario(uid, userDto) } catch (_: Exception) {}
            try { firestore.collection("users").document(uid).update("fcmToken", token).await() } catch (_: Exception) {}
        } catch (e: Exception) {
            println("No se pudo obtener/guardar token FCM: ${e.message}")
        }
    }

    // (nuevo) Cierra la sesión actual.
    // (nuevo) No es suspend porque signOut es una operación local rápida.
    fun cerrarSesion() {
        auth.signOut()
    }

    // (modificado) Actualiza perfil: Node primario, Firestore respaldo.
    suspend fun actualizarPerfil(datos: Map<String, Any>): Result<Unit> = try {
        val uid = auth.currentUser?.uid ?: throw Exception("No hay sesión activa.")

        // (nuevo) Convierte Map a UserNodeDto.
        val userDto = UserNodeDto(
            firebaseUid = uid,
            nombre = datos["nombre"] as? String ?: "",
            correo = auth.currentUser?.email ?: "",
            rol = datos["rol"] as? String ?: "Donante",
            direccion = datos["direccion"] as? String,
            telefono = datos["telefono"] as? String,
            ciudad = datos["ciudad"] as? String,
            genero = datos["genero"] as? String
        )

        // (nuevo) Escribe en Node.
        val resp = RetrofitClient.getNodeApiService().actualizarUsuario(uid, userDto)
        if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")

        // (respaldo) Firestore.
        try { firestore.collection("users").document(uid).update(datos).await() } catch (_: Exception) {}

        Result.success(Unit)
    } catch (e: Exception) {
        // (comportamiento viejo) Si Node falla, Firestore.
        try {
            val uid = auth.currentUser?.uid ?: throw Exception("No hay sesión activa.")
            firestore.collection("users").document(uid).update(datos).await()
            Result.success(Unit)
        } catch (e2: Exception) {
            Result.failure(e2)
        }
    }

    // (nuevo) Sincroniza usuario actual en MySQL (llamar al hacer login).
    suspend fun sincronizarUsuarioActual() {
        val user = auth.currentUser ?: return
        try {
            val perfil = firestore.collection("users").document(user.uid).get().await()
            val nombre = perfil.getString("nombre") ?: user.displayName ?: ""
            val rol = perfil.getString("rol") ?: "Donante"

            val userDto = UserNodeDto(
                firebaseUid = user.uid,
                nombre = nombre,
                correo = user.email ?: "",
                rol = rol
            )

            RetrofitClient.getNodeApiService().sincronizarUsuario(userDto)
            println(">>> USER SYNC OK: ${user.uid}")
        } catch (e: Exception) {
            println(">>> USER SYNC FAIL: ${e.message}")
        }
    }

    // (nuevo) Cambio de contraseña con re-autenticación (exigencia de Firebase).
    suspend fun cambiarContrasena(actual: String, nueva: String): Result<Unit> = try {
        val user = auth.currentUser ?: throw Exception("No hay sesión activa.")
        val credencial = EmailAuthProvider.getCredential(user.email ?: "", actual)
        user.reauthenticate(credencial).await()
        user.updatePassword(nueva).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}