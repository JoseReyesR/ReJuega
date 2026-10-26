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

// (nuevo) ViewModel que gestiona el estado de la pantalla Home.
// (nuevo) Recibe el Repository por constructor (Inyección de Dependencias).
class CampaignViewModel(
    private val campaignRepository: CampaignRepository
) : ViewModel() {

    // (nuevo) Estado de la UI: Comienza en "Loading" porque apenas arranca la pantalla.
    // (nuevo) MutableStateFlow permite modificar el valor internamente.
    private val _uiState = MutableStateFlow<UiState<List<Campaign>>>(UiState.Loading)

    // (nuevo) StateFlow de solo lectura: La UI solo puede leer el estado, no modificarlo.
    // (nuevo) Esto garantiza el flujo unidireccional de datos (la UI observa, el ViewModel actualiza).
    val uiState: StateFlow<UiState<List<Campaign>>> = _uiState.asStateFlow()

    // (nuevo) Bloque init: Se ejecuta cuando el ViewModel se crea por primera vez.
    init {
        // (nuevo) Cargamos los datos semilla y luego observamos las campañas de la base de datos.
        cargarCampaigns()
    }

    // (nuevo) Función que carga las campañas desde el Repository.
    // (nuevo) viewModelScope.launch: Ejecuta la corrutina asociada al ciclo de vida del ViewModel.
    // (nuevo) Cuando el ViewModel se destruye, la corrutina se cancela automáticamente (evita fugas de memoria).
    private fun cargarCampaigns() {
        viewModelScope.launch {
            try {
                // (nuevo) Paso 1: Inicializamos la base de datos con datos semilla (hardcodeados).
                // (nuevo) Esto garantiza que la pantalla no esté vacía la primera vez que se abre.
                campaignRepository.inicializarDatosSemilla()

                // (nuevo) Paso 2: Observamos el flujo de campañas de Room.
                // (nuevo) Room emitirá automáticamente una nueva lista cada vez que los datos cambien.
                campaignRepository.campaigns.collect { campaigns ->
                    // (nuevo) Si la lista está vacía, mostramos el estado Empty.
                    if (campaigns.isEmpty()) {
                        _uiState.value = UiState.Empty
                    } else {
                        // (nuevo) Si hay datos, mostramos el estado Success con la lista de campañas.
                        _uiState.value = UiState.Success(campaigns)
                    }
                }
            } catch (e: Exception) {
                // (nuevo) Si ocurre un error (ej. fallo en la base de datos), mostramos el estado Error.
                _uiState.value = UiState.Error("Error al cargar las campañas: ${e.message}")
            }
        }
    }
}

// (nuevo) Factory para crear el ViewModel inyectando el Repository.
// (nuevo) Android no sabe cómo crear un ViewModel con parámetros, así que le damos esta "fábrica".
class CampaignViewModelFactory(
    private val campaignRepository: CampaignRepository
) : ViewModelProvider.Factory {

    // (nuevo) Este método es llamado por Android cuando necesita crear una instancia del ViewModel.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // (nuevo) Verificamos que el tipo solicitado sea CampaignViewModel y lo creamos con el Repository.
        if (modelClass.isAssignableFrom(CampaignViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CampaignViewModel(campaignRepository) as T
        }
        // (nuevo) Si se solicita un tipo diferente, lanzamos una excepción.
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}