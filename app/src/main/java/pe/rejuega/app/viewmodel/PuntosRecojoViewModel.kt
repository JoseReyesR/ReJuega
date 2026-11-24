// (nuevo) ViewModel de gestión de puntos de recojo (Admin).
package pe.rejuega.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.remote.CampaignFirestore
import pe.rejuega.app.data.remote.NominatimResult
import pe.rejuega.app.data.remote.PickupEventFirestore
import pe.rejuega.app.data.remote.PuntoEntregaFirestore
import pe.rejuega.app.data.repository.CampaignFirestoreRepository
import pe.rejuega.app.data.repository.GeocodingRepository
import pe.rejuega.app.data.repository.PuntoRecojoFirestoreRepository

data class PuntosRecojoUiState(
    val isLoading: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
)

class PuntosRecojoViewModel(
    private val repository: PuntoRecojoFirestoreRepository,
    private val campaignRepository: CampaignFirestoreRepository,
    private val geocodingRepository: GeocodingRepository,
    private val uidAdmin: String
) : ViewModel() {

    private val _puntos = MutableStateFlow<List<PuntoEntregaFirestore>>(emptyList())
    val puntos: StateFlow<List<PuntoEntregaFirestore>> = _puntos.asStateFlow()

    private val _eventos = MutableStateFlow<List<PickupEventFirestore>>(emptyList())
    val eventos: StateFlow<List<PickupEventFirestore>> = _eventos.asStateFlow()

    // (nuevo) Campañas activas para el selector del formulario de eventos.
    private val _campaigns = MutableStateFlow<List<CampaignFirestore>>(emptyList())
    val campaigns: StateFlow<List<CampaignFirestore>> = _campaigns.asStateFlow()

    // (nuevo) Sugerencias de direcciones con coordenadas.
    private val _sugerencias = MutableStateFlow<List<NominatimResult>>(emptyList())
    val sugerencias: StateFlow<List<NominatimResult>> = _sugerencias.asStateFlow()

    private val _buscando = MutableStateFlow(false)
    val buscando: StateFlow<Boolean> = _buscando.asStateFlow()

    private val _uiState = MutableStateFlow(PuntosRecojoUiState())
    val uiState: StateFlow<PuntosRecojoUiState> = _uiState.asStateFlow()

    init { recargar() }

    // (modificado) recargar() re-ejecuta las lecturas (ahora son de una sola emisión).
    fun recargar() {
        viewModelScope.launch {
            try {
                repository.observarPuntos().collect { lista ->
                    _puntos.value = lista.sortedBy { it.nombre }
                }
            } catch (e: Exception) {
                println("Error observando puntos: ${e.message}")
            }
        }
        viewModelScope.launch {
            try {
                repository.observarEventos().collect { lista ->
                    _eventos.value = lista.sortedBy { it.fecha }
                }
            } catch (e: Exception) {
                println("Error observando eventos: ${e.message}")
            }
        }
        viewModelScope.launch {
            try {
                campaignRepository.obtenerCampaigns().collect { lista ->
                    _campaigns.value = lista.filter { it.estado == "Activa" }
                }
            } catch (e: Exception) {
                println("Error observando campañas: ${e.message}")
            }
        }
    }

    // (nuevo) Busca direcciones (1 petición por clic, respeta política de Nominatim).
    fun buscarDireccion(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _buscando.value = true
            try {
                _sugerencias.value = geocodingRepository.buscar(query)
            } catch (e: Exception) {
                _sugerencias.value = emptyList()
                _uiState.value = PuntosRecojoUiState(error = "No se pudo buscar la dirección.")
            }
            _buscando.value = false
        }
    }

    fun limpiarSugerencias() { _sugerencias.value = emptyList() }

    fun guardarPunto(punto: PuntoEntregaFirestore) {
        viewModelScope.launch {
            _uiState.value = PuntosRecojoUiState(isLoading = true)
            try {
                repository.guardarPunto(punto.copy(creadoPor = punto.creadoPor.ifBlank { uidAdmin }))
                _uiState.value = PuntosRecojoUiState(mensaje = "Punto guardado correctamente.")
            } catch (e: Exception) {
                _uiState.value = PuntosRecojoUiState(error = "Error: ${e.message}")
            }
        }
    }

    fun togglePunto(id: String, activo: Boolean) {
        viewModelScope.launch {
            try {
                repository.cambiarEstadoPunto(id, !activo)
            } catch (e: Exception) {
                _uiState.value = PuntosRecojoUiState(error = "Error: ${e.message}")
            }
        }
    }

    fun guardarEvento(evento: PickupEventFirestore) {
        viewModelScope.launch {
            _uiState.value = PuntosRecojoUiState(isLoading = true)
            try {
                repository.guardarEvento(evento.copy(creadoPor = evento.creadoPor.ifBlank { uidAdmin }))
                _uiState.value = PuntosRecojoUiState(mensaje = "Evento guardado correctamente.")
            } catch (e: Exception) {
                _uiState.value = PuntosRecojoUiState(error = "Error: ${e.message}")
            }
        }
    }

    fun toggleEvento(id: String, activo: Boolean) {
        viewModelScope.launch {
            try {
                repository.cambiarEstadoEvento(id, !activo)
            } catch (e: Exception) {
                _uiState.value = PuntosRecojoUiState(error = "Error: ${e.message}")
            }
        }
    }

    fun puntoPorId(id: String) = _puntos.value.find { it.id == id }
    fun eventoPorId(id: String) = _eventos.value.find { it.id == id }
    fun limpiarEstado() { _uiState.value = PuntosRecojoUiState() }
}

class PuntosRecojoViewModelFactory(
    private val repository: PuntoRecojoFirestoreRepository,
    private val campaignRepository: CampaignFirestoreRepository,
    private val geocodingRepository: GeocodingRepository,
    private val uidAdmin: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PuntosRecojoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PuntosRecojoViewModel(repository, campaignRepository, geocodingRepository, uidAdmin) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
