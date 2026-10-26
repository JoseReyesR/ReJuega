// (nuevo) Archivo: CampaignDetailViewModel.kt
// (nuevo) Descripción: ViewModel para la pantalla de detalle de campaña.
// (nuevo) Responsabilidad: Buscar una campaña por ID y exponer su estado a la UI.
package pe.rejuega.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.local.Campaign
import pe.rejuega.app.data.repository.CampaignRepository
import pe.rejuega.app.utils.UiState

// (nuevo) ViewModel para mostrar el detalle de una campaña.
class CampaignDetailViewModel(
    private val campaignRepository: CampaignRepository,
    private val campaignId: Long
) : ViewModel() {

    // (nuevo) Estado inicial de la pantalla: cargando.
    private val _uiState = MutableStateFlow<UiState<Campaign>>(UiState.Loading)

    // (nuevo) Estado público de solo lectura para la UI.
    val uiState: StateFlow<UiState<Campaign>> = _uiState.asStateFlow()

    // (nuevo) Al crear el ViewModel, se carga automáticamente la campaña.
    init {
        cargarCampaignPorId()
    }

    // (nuevo) Función que busca la campaña en el repositorio usando corrutinas.
    private fun cargarCampaignPorId() {
        viewModelScope.launch {
            try {
                // (nuevo) Se busca la campaña en la base de datos local.
                val campaign = campaignRepository.obtenerCampaignPorId(campaignId)

                // (nuevo) Si la campaña existe, se muestra Success.
                // (nuevo) Si no existe, se muestra Error.
                if (campaign != null) {
                    _uiState.value = UiState.Success(campaign)
                } else {
                    _uiState.value = UiState.Error("No se encontró la campaña seleccionada.")
                }
            } catch (e: Exception) {
                // (nuevo) Si ocurre una excepción, se muestra un mensaje amigable.
                _uiState.value = UiState.Error("Error al cargar la campaña: ${e.message}")
            }
        }
    }
}

// (nuevo) Factory para crear el ViewModel con dependencias y parámetros.
class CampaignDetailViewModelFactory(
    private val campaignRepository: CampaignRepository,
    private val campaignId: Long
) : ViewModelProvider.Factory {

    // (nuevo) Android llama a este método cuando necesita crear el ViewModel.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CampaignDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CampaignDetailViewModel(campaignRepository, campaignId) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}