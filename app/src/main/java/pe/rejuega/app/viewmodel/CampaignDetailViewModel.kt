// (nuevo) Archivo: CampaignDetailViewModel.kt
// (nuevo) Descripción: ViewModel para la pantalla de detalle de campaña (Node + MySQL).
package pe.rejuega.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.remote.CampaignFirestore
import pe.rejuega.app.data.repository.CampaignFirestoreRepository
import pe.rejuega.app.utils.UiState

// (modificado) Usa CampaignFirestoreRepository (Node + MySQL) en lugar de CampaignRepository (Room).
class CampaignDetailViewModel(
    private val campaignRepository: CampaignFirestoreRepository,
    private val firestoreId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<CampaignFirestore>>(UiState.Loading)
    val uiState: StateFlow<UiState<CampaignFirestore>> = _uiState.asStateFlow()

    init {
        cargarCampaign()
    }

    fun cargarCampaign() {
        println(">>> REFRESH-D: recargando campaña firestoreId=$firestoreId")
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                println(">>> REFRESH-D: llamando a campaignRepository.obtenerPorId($firestoreId)")
                val campaign = campaignRepository.obtenerPorId(firestoreId)
                println(">>> REFRESH-D: resultado = ${if (campaign != null) "campaña encontrada: ${campaign.titulo}" else "NULL"}")
                _uiState.value = if (campaign != null) {
                    UiState.Success(campaign)
                } else {
                    UiState.Error("No se encontró la campaña seleccionada.")
                }
                println(">>> REFRESH-D: uiState actualizado a ${_uiState.value::class.simpleName}")
            } catch (e: Exception) {
                println(">>> REFRESH-D: ERROR en cargarCampaign: ${e.message}")
                e.printStackTrace()
                _uiState.value = UiState.Error("Error al cargar la campaña: ${e.message}")
            }
        }
    }
}

// (modificado) Factory con firestoreId (String) en lugar de campaignId (Long).
class CampaignDetailViewModelFactory(
    private val campaignRepository: CampaignFirestoreRepository,
    private val firestoreId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CampaignDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CampaignDetailViewModel(campaignRepository, firestoreId) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
