// (nuevo) ViewModel independiente para la pantalla de Huella Ecológica.
// (nuevo) Calcula el CO₂ real basado en donaciones APROBADAS/ENTREGADAS del usuario.
package pe.rejuega.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.repository.DonationFirestoreRepository

data class ImpactoAmbientalUiState(
    val isLoading: Boolean = false,
    val donacionesValidas: Int = 0,      // (nuevo) Solo Aprobadas o Entregadas
    val co2TotalKg: Double = 0.0,        // (nuevo) Suma real del CO₂ ahorrado
    val error: String? = null
)

class ImpactoAmbientalViewModel(
    private val donationRepository: DonationFirestoreRepository,
    private val uid: String
) : ViewModel() {

    private val _state = MutableStateFlow(ImpactoAmbientalUiState(isLoading = true))
    val state: StateFlow<ImpactoAmbientalUiState> = _state.asStateFlow()

    init { calcularImpacto() }

    // (nuevo) Calcula impacto real desde el servidor (con respaldo a Firestore).
    private fun calcularImpacto() {
        viewModelScope.launch {
            _state.value = ImpactoAmbientalUiState(isLoading = true)
            try {
                // (nuevo) Llama al repository que ya lee de Node con respaldo a Firestore.
                val todasDonaciones = donationRepository.obtenerPorUsuarioUnaVez(uid)

                // (nuevo) SOLO contar Aprobadas y Entregadas (las que realmente llegaron al niño).
                val validas = todasDonaciones.filter {
                    it.estadoTrazabilidad == "Aprobado" || it.estadoTrazabilidad == "Entregado"
                }

                val co2Total = validas.sumOf { it.co2AhorradoKg }

                println(">>> IMPACTO: total=${todasDonaciones.size} validas=${validas.size} co2=$co2Total")

                _state.value = ImpactoAmbientalUiState(
                    donacionesValidas = validas.size,
                    co2TotalKg = co2Total
                )
            } catch (e: Exception) {
                _state.value = ImpactoAmbientalUiState(error = "Error al calcular impacto: ${e.message}")
            }
        }
    }

    fun recalcular() = calcularImpacto()
}

class ImpactoAmbientalViewModelFactory(
    private val donationRepository: DonationFirestoreRepository,
    private val uid: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImpactoAmbientalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImpactoAmbientalViewModel(donationRepository, uid) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
