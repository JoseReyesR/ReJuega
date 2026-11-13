// (nuevo) Archivo: CampaignViewModel.kt
// (nuevo) Descripción: ViewModel para la pantalla Home (lista de campañas).
// (nuevo) Responsabilidad: Obtener las campañas del Repository y exponerlas a la UI.
// (nuevo) Patrón: MVVM - Este ViewModel NO conoce los detalles de Room, solo usa el Repository.
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

import pe.rejuega.app.data.repository.CampaignFirestoreRepository
import pe.rejuega.app.data.remote.CampaignFirestore



// (nuevo) ViewModel que gestiona el estado de la pantalla Home.
// (nuevo) Recibe el Repository por constructor (Inyección de Dependencias).
class CampaignViewModel(
    private val campaignRepository: CampaignRepository,        // (nuevo) Room (caché)
    private val campaignFirestoreRepository: CampaignFirestoreRepository  // (nuevo) Firestore

) : ViewModel() {

    // (nuevo) Estado de la UI: Comienza en "Loading" porque apenas arranca la pantalla.
    // (nuevo) MutableStateFlow permite modificar el valor internamente.
    private val _uiState = MutableStateFlow<UiState<List<Campaign>>>(UiState.Loading)

    //  StateFlow de solo lectura: La UI solo puede leer el estado, no modificarlo.
    // Esto garantiza el flujo unidireccional de datos (la UI observa, el ViewModel actualiza).
    val uiState: StateFlow<UiState<List<Campaign>>> = _uiState.asStateFlow()

    // Bloque init: Se ejecuta cuando el ViewModel se crea por primera vez.
    init {
        // Cargamos los datos semilla y luego observamos las campañas de la base de datos.
        cargarCampaigns()
    }

    //  Función que carga las campañas desde el Repository.
    //  viewModelScope.launch: Ejecuta la corrutina asociada al ciclo de vida del ViewModel.
    //  Cuando el ViewModel se destruye, la corrutina se cancela automáticamente.
    private fun cargarCampaigns() {
        viewModelScope.launch {
            try {
                // (nuevo) Lee campañas de Firestore en tiempo real.
                campaignFirestoreRepository.obtenerCampaigns().collect { campaigns ->
                    if (campaigns.isEmpty()) {
                        _uiState.value = UiState.Empty
                    } else {
                        // (nuevo) Mapea a Campaign (modelo de UI).
                        val mapeadas = campaigns.filter { it.estado == "Activa" }.map { it.toCampaign() }
                        _uiState.value = if (mapeadas.isEmpty()) UiState.Empty else UiState.Success(mapeadas)
                    }
                }
            } catch (e: Exception) {
                // (nuevo) Si ocurre un error (ej. fallo en la base de datos), mostramos el estado Error.
                _uiState.value = UiState.Error("Error al cargar las campañas: ${e.message}")
            }
        }
    }
}


// (nuevo) Función de mapeo Firestore -> UI.
fun CampaignFirestore.toCampaign(): Campaign {
    return Campaign(
        id = 0, titulo = titulo, descripcion = descripcion,
        fechaInicio = fechaInicio, fechaFin = fechaFin,
        estado = estado, imagenUrl = imagenUrl
    )
}
// (nuevo) Factory para crear el ViewModel inyectando el Repository.
// (nuevo) Android no sabe cómo crear un ViewModel con parámetros, así que le damos esta "fábrica".
class CampaignViewModelFactory(
    private val campaignRepository: CampaignRepository,
    private val campaignFirestoreRepository: CampaignFirestoreRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CampaignViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CampaignViewModel(campaignRepository, campaignFirestoreRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}