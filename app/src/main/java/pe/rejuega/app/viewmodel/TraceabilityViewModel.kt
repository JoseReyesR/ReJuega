// (modificado) La trazabilidad ahora lee desde Firestore por uid (HU-07 criterio 2).
// (modificado) El listener en tiempo real cumple el criterio 3 (reflejo al cambiar).
package pe.rejuega.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.remote.Donacion
import pe.rejuega.app.data.repository.DonationFirestoreRepository

class TraceabilityViewModel(
    private val repository: DonationFirestoreRepository,
    private val uid: String
) : ViewModel() {

    private val _donaciones = MutableStateFlow<List<Donacion>>(emptyList())
    val donaciones: StateFlow<List<Donacion>> = _donaciones.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { observar() }

    fun recargar() {
        observar()
    }

    fun eliminarDonacion(id: String) {
        viewModelScope.launch {
            try {
                repository.eliminarDonacion(id)
                pe.rejuega.app.utils.RefreshTrigger.notificar()
                recargar()
            } catch (e: Exception) {
                _error.value = "Error al eliminar donación: ${e.message}"
            }
        }
    }

    private fun observar() {
        viewModelScope.launch {
            try {
                repository.obtenerPorUsuario(uid).collect { lista ->
                    _donaciones.value = lista.sortedByDescending { it.fechaRegistro }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = "Error al cargar tus donaciones: ${e.message}"
            }
        }
    }
}

// (modificado) Factory con repository y uid.
class TraceabilityViewModelFactory(
    private val repository: DonationFirestoreRepository,
    private val uid: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TraceabilityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TraceabilityViewModel(repository, uid) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}