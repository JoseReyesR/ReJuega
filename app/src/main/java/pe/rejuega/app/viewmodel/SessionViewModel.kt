// (nuevo) Descripción: ViewModel que mantiene el estado de la sesión del usuario actual.
// (nuevo) Responsabilidad: exponer el usuario autenticado y su rol a toda la app,
// (nuevo) para que la UI pueda mostrar/ocultar funcionalidades según el rol.
// (nuevo) Implementa RN-04: Control de acceso por rol.
package pe.rejuega.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.rejuega.app.data.remote.AuthRepository
import pe.rejuega.app.domain.model.Usuario

// (nuevo) Estado de la sesión del usuario.
data class SessionUiState(
    // (nuevo) El usuario autenticado actual. Null si no hay sesión.
    val usuario: Usuario? = null,
    // (nuevo) Indica si estamos cargando los datos del usuario.
    val isLoading: Boolean = false
)

// (nuevo) ViewModel que gestiona la sesión del usuario a nivel de toda la app.
// (nuevo) Se crea a nivel de Activity para que persista entre pantallas.
class SessionViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // (nuevo) Estado interno de la sesión.
    private val _sessionState = MutableStateFlow(SessionUiState())

    // (nuevo) Estado público de solo lectura para la UI.
    val sessionState: StateFlow<SessionUiState> = _sessionState.asStateFlow()

    // (nuevo) Al crear el ViewModel, cargamos el usuario actual si existe sesión.
    init {
        cargarUsuarioActual()
    }

    // (nuevo) Carga el usuario actual desde Firebase Auth + Firestore.
    // (nuevo) Se llama en el init y también después de un login exitoso.
    fun cargarUsuarioActual() {
        viewModelScope.launch {
            _sessionState.update { it.copy(isLoading = true) }

            try {
                // (nuevo) Obtiene el usuario desde AuthRepository.
                // (nuevo) Este método lee Firebase Auth y luego el documento de Firestore.
                val usuario = authRepository.obtenerUsuarioActual()

                _sessionState.update {
                    it.copy(
                        usuario = usuario,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // (nuevo) Si falla la carga, dejamos el usuario en null.
                _sessionState.update {
                    it.copy(
                        usuario = null,
                        isLoading = false
                    )
                }
            }
        }
    }

    // (nuevo) Verifica si el usuario actual tiene rol de Administrador.
    // (nuevo) Implementa RN-04: las acciones de administración se restringen por rol.
    fun esAdministrador(): Boolean {
        return _sessionState.value.usuario?.esAdministrador() == true
    }

    // (nuevo) Devuelve el rol actual como String ("Donante", "Gestor" o "Administrador").
    fun obtenerRolActual(): String {
        return _sessionState.value.usuario?.rol ?: "Donante"
    }

    // (nuevo) ¿Es Gestor o Administrador? (ambos ven el panel de gestión).
    fun esGestorOAdmin(): Boolean {
        val rol = obtenerRolActual()
        return rol == "Gestor" || rol == "Administrador"
    }

    // (nuevo) Limpia la sesión (se llama al cerrar sesión).
    fun cerrarSesion() {
        _sessionState.update {
            SessionUiState(
                usuario = null,
                isLoading = false
            )
        }
    }
}

// (nuevo) Factory para inyectar el AuthRepository al SessionViewModel.
class SessionViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionViewModel(authRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}