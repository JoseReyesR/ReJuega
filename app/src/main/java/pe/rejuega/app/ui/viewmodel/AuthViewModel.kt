// (nuevo) Archivo: AuthViewModel.kt
// (nuevo) Descripción: ViewModel para Login y Registro.
// (nuevo) Implementa HU-01: autenticación de usuarios por rol.
package pe.rejuega.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.rejuega.app.data.remote.AuthRepository
import pe.rejuega.app.domain.model.Usuario

// (nuevo) Estado de la interfaz de autenticación.
data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val usuario: Usuario? = null,
    val autenticado: Boolean = false
)

// (nuevo) ViewModel que gestiona login, registro y sesión actual.
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // (nuevo) Estado interno modificable.
    private val _uiState = MutableStateFlow(AuthUiState())

    // (nuevo) Estado público de solo lectura.
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // (nuevo) Al crear el ViewModel, verificamos si ya existe una sesión activa.
    init {
        verificarSesionActual()
    }

    // (nuevo) Verifica si Firebase Authentication ya tiene un usuario conectado.
    private fun verificarSesionActual() {
        viewModelScope.launch {
            try {
                val usuario = authRepository.obtenerUsuarioActual()

                if (usuario != null) {
                    _uiState.update {
                        it.copy(
                            usuario = usuario,
                            autenticado = true,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                // (nuevo) Si no se puede verificar la sesión, no mostramos error inmediato.
                // (nuevo) El usuario igual puede iniciar sesión manualmente.
            }
        }
    }

    // (nuevo) Inicia sesión con correo y contraseña.
    fun iniciarSesion(correo: String, password: String) {

        // (nuevo) Validación básica de campos vacíos.
        if (correo.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(error = "Ingresa correo y contraseña.")
            }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(isLoading = true, error = null)
                }

                // (nuevo) Llamamos al repositorio.
                authRepository.iniciarSesion(correo.trim(), password)

                // (nuevo) Obtenemos el perfil del usuario desde Firestore.
                val usuario = authRepository.obtenerUsuarioActual()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        usuario = usuario,
                        autenticado = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = obtenerMensajeError(e)
                    )
                }
            }
        }
    }

    // (nuevo) Registra un usuario nuevo.
    fun registrarUsuario(nombre: String, correo: String, password: String) {

        // (nuevo) Validación básica de campos.
        if (nombre.isBlank() || correo.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(error = "Completa nombre, correo y contraseña.")
            }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(isLoading = true, error = null)
                }

                // (nuevo) Registramos al usuario.
                authRepository.registrarUsuario(
                    nombre = nombre.trim(),
                    correo = correo.trim(),
                    password = password
                )

                // (nuevo) Después del registro, obtenemos el usuario actual.
                val usuario = authRepository.obtenerUsuarioActual()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        usuario = usuario,
                        autenticado = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = obtenerMensajeError(e)
                    )
                }
            }
        }
    }

    // (nuevo) Cierra la sesión actual.
    fun cerrarSesion() {
        authRepository.cerrarSesion()

        _uiState.update {
            AuthUiState(
                isLoading = false,
                error = null,
                usuario = null,
                autenticado = false
            )
        }
    }

    // (nuevo) Limpia el mensaje de error.
    fun limpiarError() {
        _uiState.update {
            it.copy(error = null)
        }
    }

    // (nuevo) Convierte excepciones comunes de Firebase en mensajes amigables.
    private fun obtenerMensajeError(e: Exception): String {
        return when (e) {
            is FirebaseAuthWeakPasswordException ->
                "La contraseña debe tener al menos 6 caracteres."

            is FirebaseAuthInvalidCredentialsException ->
                "Correo o contraseña incorrectos."

            is FirebaseAuthUserCollisionException ->
                "Este correo ya está registrado. Intenta iniciar sesión."

            else ->
                e.message ?: "Ocurrió un error de autenticación."
        }
    }
}

// (nuevo) Factory para inyectar el repositorio al ViewModel.
class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}