// (nuevo) Archivo: TraceabilityViewModel.kt
// (nuevo) Descripción: ViewModel de la pantalla de trazabilidad (HU-06).
// (nuevo) Responsabilidad: exponer la lista de donaciones y orquestar el cambio de estado.
package pe.rejuega.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.local.Juguete
import pe.rejuega.app.data.repository.JugueteRepository

// (nuevo) ViewModel que observa los juguetes guardados localmente en Room.
class TraceabilityViewModel(
    private val jugueteRepository: JugueteRepository
) : ViewModel() {

    // (nuevo) Lista interna de donaciones. Empieza vacía mientras Room responde.
    private val _juguetes = MutableStateFlow<List<Juguete>>(emptyList())

    // (nuevo) Lista pública de solo lectura para la pantalla.
    val juguetes: StateFlow<List<Juguete>> = _juguetes.asStateFlow()

    // (nuevo) Bandera para mostrar el CircularProgressIndicator.
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // (nuevo) Mensaje de error visible, si existe.
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    // (nuevo) Se ejecuta la primera vez que se crea este ViewModel.
    init {
        observarJuguetes()
    }

    // (nuevo) Se suscribe al Flow del repositorio.
    // (nuevo) viewModelScope cancela la corrutina automáticamente al destruirse el ViewModel.
    private fun observarJuguetes() {
        viewModelScope.launch {
            try {
                jugueteRepository.juguetes.collect { lista ->
                    _juguetes.value = lista
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                // (nuevo) Si falla la BD, dejamos de cargar y mostramos un error.
                _isLoading.value = false
                _mensajeError.value = "No se pudieron cargar las donaciones: ${e.message}"
            }
        }
    }

    // (nuevo) Solicita avanzar un juguete al siguiente estado.
    // (nuevo) La validación real vive en el Repository, no aquí ni en la UI.
    fun avanzarEstado(id: Long) {
        viewModelScope.launch {
            try {
                // (nuevo) Limpiamos errores anteriores antes de intentar de nuevo.
                _mensajeError.value = null

                // (nuevo) Ejecutamos la operación en segundo plano.
                jugueteRepository.avanzarEstadoJuguete(id)

                // (nuevo) No hace falta recargar la lista: el Flow de Room ya la actualiza.
            } catch (e: Exception) {
                // (nuevo) Mostramos el mensaje controlado del Repository.
                _mensajeError.value = e.message
            }
        }
    }

    // (nuevo) Permite cerrar el mensaje de error desde la UI.
    fun limpiarError() {
        _mensajeError.value = null
    }
}

// (nuevo) Factory para inyectar el repositorio al crear el ViewModel.
class TraceabilityViewModelFactory(
    private val jugueteRepository: JugueteRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TraceabilityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TraceabilityViewModel(jugueteRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}