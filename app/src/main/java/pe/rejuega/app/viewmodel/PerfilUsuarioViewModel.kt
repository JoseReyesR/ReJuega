// (nuevo) ViewModel del perfil: datos editables, cambio de contraseña y CO₂ real.
package pe.rejuega.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.remote.AuthRepository
import pe.rejuega.app.data.repository.DonationFirestoreRepository

data class PerfilUsuarioUiState(
    val nombre: String = "",
    val correo: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val ciudad: String = "",
    val genero: String = "",
    val isLoading: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
)

class PerfilUsuarioViewModel(
    private val authRepository: AuthRepository,
    private val donationRepository: DonationFirestoreRepository,
    private val uid: String
) : ViewModel() {

    private val _state = MutableStateFlow(PerfilUsuarioUiState())
    val state: StateFlow<PerfilUsuarioUiState> = _state.asStateFlow()

    // (nuevo) CO₂ real: suma del co2AhorradoKg de las donaciones del usuario.
    private val _co2Donaciones = MutableStateFlow(0.0)
    val co2Donaciones: StateFlow<Double> = _co2Donaciones.asStateFlow()

    init {
        cargarPerfil()
        cargarCo2()
    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            val usuario = authRepository.obtenerUsuarioActual()
            usuario?.let { u ->
                _state.value = _state.value.copy(
                    nombre = u.nombre, correo = u.correo, direccion = u.direccion,
                    telefono = u.telefono, ciudad = u.ciudad, genero = u.genero
                )
            }
        }
    }

    private fun cargarCo2() {
        viewModelScope.launch {
            try {
                donationRepository.obtenerPorUsuarioUnaVez(uid).let { lista ->
                    _co2Donaciones.value = lista.sumOf { it.co2AhorradoKg }
                }
            } catch (e: Exception) { /* sin conexión: se muestra 0 */ }
        }
    }

    fun onCampoChange(campo: String, valor: String) {
        _state.value = when (campo) {
            "nombre" -> _state.value.copy(nombre = valor)
            "direccion" -> _state.value.copy(direccion = valor)
            "telefono" -> _state.value.copy(telefono = valor)
            "ciudad" -> _state.value.copy(ciudad = valor)
            "genero" -> _state.value.copy(genero = valor)
            else -> _state.value
        }
    }

    // (nuevo) Guarda los datos en users/{uid}.
    fun guardarPerfil() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val resultado = authRepository.actualizarPerfil(
                mapOf(
                    "nombre" to _state.value.nombre.trim(),
                    "direccion" to _state.value.direccion.trim(),
                    "telefono" to _state.value.telefono.trim(),
                    "ciudad" to _state.value.ciudad.trim(),
                    "genero" to _state.value.genero
                )
            )
            resultado.onSuccess {
                _state.value = _state.value.copy(isLoading = false, mensaje = "Perfil actualizado.")
            }.onFailure { e ->
                _state.value = _state.value.copy(isLoading = false, error = "Error: ${e.message}")
            }
        }
    }

    // (nuevo) Cambio de contraseña con validaciones.
    fun cambiarContrasena(actual: String, nueva: String, confirmacion: String) {
        when {
            actual.isBlank() -> _state.value = _state.value.copy(error = "Ingresa tu contraseña actual.")
            nueva.length < 6 -> _state.value = _state.value.copy(error = "La nueva contraseña debe tener al menos 6 caracteres.")
            nueva != confirmacion -> _state.value = _state.value.copy(error = "La confirmación no coincide.")
            else -> viewModelScope.launch {
                _state.value = _state.value.copy(isLoading = true, error = null)
                authRepository.cambiarContrasena(actual, nueva)
                    .onSuccess { _state.value = _state.value.copy(isLoading = false, mensaje = "Contraseña actualizada correctamente.") }
                    .onFailure { e -> _state.value = _state.value.copy(isLoading = false, error = "Contraseña actual incorrecta o error: ${e.message}") }
            }
        }
    }

    fun limpiarMensaje() { _state.value = _state.value.copy(mensaje = null, error = null) }
}

class PerfilUsuarioViewModelFactory(
    private val authRepository: AuthRepository,
    private val donationRepository: DonationFirestoreRepository,
    private val uid: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PerfilUsuarioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PerfilUsuarioViewModel(authRepository, donationRepository, uid) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
