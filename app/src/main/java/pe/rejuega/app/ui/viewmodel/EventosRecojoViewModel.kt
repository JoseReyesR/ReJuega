// (nuevo) ViewModel independiente que observa los eventos de recojo de una campaña.
// (nuevo) No depende del CampaignDetailViewModel: recibe el firestoreId directo.
package pe.rejuega.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.remote.PickupEventFirestore
import pe.rejuega.app.data.repository.PuntoRecojoFirestoreRepository

class EventosRecojoViewModel(
    repository: PuntoRecojoFirestoreRepository,
    campaignFirestoreId: String
) : ViewModel() {

    private val _eventos = MutableStateFlow<List<PickupEventFirestore>>(emptyList())
    val eventos: StateFlow<List<PickupEventFirestore>> = _eventos.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.observarEventosPorCampaign(campaignFirestoreId).collect { lista ->
                    _eventos.value = lista.sortedBy { it.fecha }
                }
            } catch (e: Exception) {
                println("Error observando eventos de recojo: ${e.message}")
            }
        }
    }
}

class EventosRecojoViewModelFactory(
    private val repository: PuntoRecojoFirestoreRepository,
    private val campaignFirestoreId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventosRecojoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventosRecojoViewModel(repository, campaignFirestoreId) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
