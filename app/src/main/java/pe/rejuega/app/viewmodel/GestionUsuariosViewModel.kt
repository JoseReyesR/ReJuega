// (nuevo) ViewModel para listar usuarios y cambiar roles (solo Admin puede cambiar).
package pe.rejuega.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.remote.AuthRepository
import pe.rejuega.app.data.remote.RetrofitClient
import pe.rejuega.app.data.remote.UserNodeDto

class GestionUsuariosViewModel(
    private val authRepository: AuthRepository,
    private val uidActual: String,
    private val rolActual: String
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val usuarios: List<UserNodeDto> = emptyList(),
        val mensaje: String? = null,
        val error: String? = null,
        val puedeCambiarRoles: Boolean = false
    )

    private val _state = MutableStateFlow(UiState(puedeCambiarRoles = rolActual == "Administrador"))
    val state: StateFlow<UiState> = _state.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val lista = authRepository.obtenerUsuariosUnaVez()
                    .sortedBy { it.nombre }
                _state.value = _state.value.copy(isLoading = false, usuarios = lista)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    // (modificado) Solo permite cambiar rol si el server está disponible.
    fun cambiarRol(uid: String, nuevoRol: String) {
        if (rolActual != "Administrador") {
            _state.value = _state.value.copy(error = "Solo el Administrador puede cambiar roles")
            return
        }

        viewModelScope.launch {
            // (nuevo) Verificar que el server esté disponible antes de intentar cambiar.
            val serverDisponible = try {
                val resp = RetrofitClient.getNodeApiService().obtenerMateriales()
                resp.isSuccessful
            } catch (e: Exception) {
                false
            }

            if (!serverDisponible) {
                _state.value = _state.value.copy(
                    error = "Servidor no disponible. No puedes cambiar roles hasta que se restablezca la conexión."
                )
                return@launch
            }

            // (nuevo) Server está ON, proceder con el cambio.
            val resultado = authRepository.cambiarRol(uid, nuevoRol, uidActual)
            resultado.fold(
                onSuccess = {
                    pe.rejuega.app.utils.RefreshTrigger.notificar()
                    _state.value = _state.value.copy(mensaje = "Rol actualizado correctamente en MySQL y Firestore")
                    cargar()
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(error = extractError(e.message))
                }
            )
        }
    }

    private fun extractError(raw: String?): String {
        if (raw == null) return "Error desconocido"
        return try {
            val json = org.json.JSONObject(raw)
            json.optString("error", raw)
        } catch (e: Exception) { raw }
    }

    fun limpiarMensaje() { _state.value = _state.value.copy(mensaje = null, error = null) }
}

class GestionUsuariosViewModelFactory(
    private val authRepository: AuthRepository,
    private val uidActual: String,
    private val rolActual: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionUsuariosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GestionUsuariosViewModel(authRepository, uidActual, rolActual) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
