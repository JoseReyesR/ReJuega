// (nuevo) ViewModel para el módulo de validación de donaciones del Admin.
package pe.rejuega.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.remote.Donacion
import pe.rejuega.app.data.repository.DonationFirestoreRepository
import pe.rejuega.app.utils.EstadosTrazabilidad

class DonationAdminViewModel(
    private val repository: DonationFirestoreRepository,
    private val uidAdmin: String          // (nuevo)
) : ViewModel() {

    // (nuevo) Lista completa de donaciones.
    private val _donations = MutableStateFlow<List<Donacion>>(emptyList())
    val donations: StateFlow<List<Donacion>> = _donations.asStateFlow()

    // (nuevo) Donación seleccionada para detalle.
    private val _donacionSeleccionada = MutableStateFlow<Donacion?>(null)
    val donacionSeleccionada: StateFlow<Donacion?> = _donacionSeleccionada.asStateFlow()

    // (nuevo) Estado de operaciones.
    private val _uiState = MutableStateFlow<DonationAdminUiState>(DonationAdminUiState.Idle)
    val uiState: StateFlow<DonationAdminUiState> = _uiState.asStateFlow()

    init {
        observarDonaciones()
    }

    private fun observarDonaciones() {
        viewModelScope.launch {
            repository.obtenerTodas().collect { lista ->
                _donations.value = lista
            }
        }
    }

    // (nuevo) Filtra donaciones pendientes o en revisión.
    fun obtenerPendientes(): List<Donacion> {
        return _donations.value.filter {
            it.estadoTrazabilidad == "Pendiente" || it.estadoTrazabilidad == "En Revision"
        }
    }

    // (nuevo) Selecciona una donación para ver su detalle.
    fun seleccionarDonacion(donacion: Donacion) {
        _donacionSeleccionada.value = donacion
    }


    // (modificado) Pasa el uid al repositorio.
    fun avanzarEstado(docId: String, estadoActual: String) {
        viewModelScope.launch {
            _uiState.value = DonationAdminUiState.Loading
            try {
                val siguiente = EstadosTrazabilidad.siguienteDe(estadoActual)
                if (siguiente != null) {
                    repository.actualizarEstado(docId, siguiente, uidAdmin)
                    _uiState.value = DonationAdminUiState.Success("Estado actualizado a: $siguiente")
                } else {
                    _uiState.value = DonationAdminUiState.Error("No se puede avanzar más.")
                }
            } catch (e: Exception) {
                _uiState.value = DonationAdminUiState.Error("Error: ${e.message}")
            }
        }
    }

    // (modificado) Pasa el uid al repositorio.
    fun rechazar(docId: String, motivo: String) {
        viewModelScope.launch {
            _uiState.value = DonationAdminUiState.Loading
            try {
                repository.rechazarDonacion(docId, motivo, uidAdmin)
                _uiState.value = DonationAdminUiState.Success("Donación rechazada.")
            } catch (e: Exception) {
                _uiState.value = DonationAdminUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun limpiarEstado() {
        _uiState.value = DonationAdminUiState.Idle
    }
}

// (nuevo) Estados de UI del módulo de validación.
sealed class DonationAdminUiState {
    data object Idle : DonationAdminUiState()
    data object Loading : DonationAdminUiState()
    data class Success(val mensaje: String) : DonationAdminUiState()
    data class Error(val mensaje: String) : DonationAdminUiState()
}

// (modificado) Factory con uidAdmin.
class DonationAdminViewModelFactory(
    private val repository: DonationFirestoreRepository,
    private val uidAdmin: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DonationAdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DonationAdminViewModel(repository, uidAdmin) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}