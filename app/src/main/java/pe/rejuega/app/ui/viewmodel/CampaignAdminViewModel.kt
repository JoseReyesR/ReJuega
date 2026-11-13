// (nuevo) ViewModel para el CRUD de campañas del Admin.
package pe.rejuega.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.remote.CampaignFirestore
import pe.rejuega.app.data.repository.CampaignFirestoreRepository

// (nuevo) Estado de las operaciones de campaña.
data class CampaignAdminUiState(
    val isLoading: Boolean = false,
    val mensajeExito: String? = null,
    val error: String? = null
)

class CampaignAdminViewModel(
    private val repository: CampaignFirestoreRepository,
    private val uidAdmin: String
) : ViewModel() {

    // (nuevo) Lista de campañas observada por la pantalla.
    private val _campaigns = MutableStateFlow<List<CampaignFirestore>>(emptyList())
    val campaigns: StateFlow<List<CampaignFirestore>> = _campaigns.asStateFlow()

    // (nuevo) Estado de las operaciones.
    private val _uiState = MutableStateFlow(CampaignAdminUiState())
    val uiState: StateFlow<CampaignAdminUiState> = _uiState.asStateFlow()

    init {
        observarCampaigns()
    }

    // (nuevo) Observa cambios en Firestore en tiempo real.
    private fun observarCampaigns() {
        viewModelScope.launch {
            try {
                repository.obtenerCampaigns().collect { lista ->
                    _campaigns.value = lista
                }
            } catch (e: Exception) {
                _uiState.value = CampaignAdminUiState(
                    error = "Error al obtener campañas de Firestore: ${e.message}"
                )
            }
        }
    }

    // (nuevo) Crea una campaña.
    fun crearCampaign(
        titulo: String, descripcion: String,
        fechaInicio: String, fechaFin: String, imagenUrl: String
    ) {
        viewModelScope.launch {
            _uiState.value = CampaignAdminUiState(isLoading = true)
            try {
                val campaign = CampaignFirestore(
                    titulo = titulo, descripcion = descripcion,
                    fechaInicio = fechaInicio, fechaFin = fechaFin,
                    estado = "Activa", imagenUrl = imagenUrl,
                    creadoPor = uidAdmin, fechaCreacion = System.currentTimeMillis()
                )
                repository.crearCampaign(campaign)
                _uiState.value = CampaignAdminUiState(mensajeExito = "Campaña creada correctamente.")
            } catch (e: Exception) {
                _uiState.value = CampaignAdminUiState(error = "Error al crear: ${e.message}")
            }
        }
    }

    // (nuevo) Actualiza una campaña existente.
    fun actualizarCampaign(campaign: CampaignFirestore) {
        viewModelScope.launch {
            _uiState.value = CampaignAdminUiState(isLoading = true)
            try {
                repository.actualizarCampaign(campaign)
                _uiState.value = CampaignAdminUiState(mensajeExito = "Campaña actualizada.")
            } catch (e: Exception) {
                _uiState.value = CampaignAdminUiState(error = "Error al actualizar: ${e.message}")
            }
        }
    }

    // (nuevo) Cierra una campaña.
    fun cerrarCampaign(id: String) {
        viewModelScope.launch {
            try {
                repository.cerrarCampaign(id)
            } catch (e: Exception) {
                _uiState.value = CampaignAdminUiState(error = "Error al cerrar: ${e.message}")
            }
        }
    }

    // (nuevo) Busca una campaña por ID.
    fun obtenerCampaignPorId(id: String): CampaignFirestore? {
        return _campaigns.value.find { it.id == id }
    }

    // (nuevo) Limpia el estado.
    fun limpiarEstado() {
        _uiState.value = CampaignAdminUiState()
    }
}

// (nuevo) Factory del ViewModel.
class CampaignAdminViewModelFactory(
    private val repository: CampaignFirestoreRepository,
    private val uidAdmin: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CampaignAdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CampaignAdminViewModel(repository, uidAdmin) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}