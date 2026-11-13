// (modificado) Archivo: ui/viewmodel/CampaignViewModel.kt
// (modificado) Cambio: Ahora usa CampaignSyncRepository para sincronización offline.
package pe.rejuega.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.local.Campaign
import pe.rejuega.app.data.repository.CampaignSyncRepository
import pe.rejuega.app.utils.UiState

class CampaignViewModel(
    private val campaignSyncRepository: CampaignSyncRepository
) : ViewModel() {

    // (nuevo) Estado de la lista de campañas.
    private val _uiState = MutableStateFlow<UiState<List<Campaign>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Campaign>>> = _uiState.asStateFlow()

    // (nuevo) Indica si la última sincronización fue exitosa.
    // (nuevo) La UI usa esto para mostrar un aviso de "modo offline".
    private val _estaSincronizado = MutableStateFlow(true)
    val estaSincronizado: StateFlow<Boolean> = _estaSincronizado.asStateFlow()

    init {
        cargarCampaigns()
    }

    // (nuevo) Carga campañas con estrategia Cache-First, Network-Refresh.
    private fun cargarCampaigns() {
        viewModelScope.launch {
            try {
                campaignSyncRepository.obtenerCampaigns().collect { campaigns ->
                    // (nuevo) Filtrar solo campañas activas para el Donante.
                    val activas = campaigns.filter { it.estado == "Activa" }
                    if (activas.isEmpty()) {
                        _uiState.value = UiState.Empty
                    } else {
                        _uiState.value = UiState.Success(activas)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al cargar campañas: ${e.message}")
            }
        }
    }

    // (nuevo) Fuerza una sincronización manual (botón de refrescar).
    fun refrescar() {
        viewModelScope.launch {
            try {
                campaignSyncRepository.sincronizar()
                _estaSincronizado.value = true
                // (nuevo) Recargar después de sincronizar.
                cargarCampaigns()
            } catch (e: Exception) {
                // (nuevo) Sin conexión: marcamos como no sincronizado.
                _estaSincronizado.value = false
            }
        }
    }

}

// (modificado) Factory actualizado para el nuevo repository.
class CampaignViewModelFactory(
    private val campaignSyncRepository: CampaignSyncRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CampaignViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CampaignViewModel(campaignSyncRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}