// (nuevo) ViewModel para la pantalla de Mi Huella Ecológica (cálculo real).
package pe.rejuega.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.repository.DonationFirestoreRepository

// (modificado) UiState con desglose por estado para transparencia.
data class MiHuellaEcologicaUiState(
    val isLoading: Boolean = false,
    val donacionesValidas: Int = 0,
    val co2TotalKg: Double = 0.0,
    // (nuevo) Desglose por estado.
    val pendientes: Int = 0,
    val enRevision: Int = 0,
    val aprobados: Int = 0,
    val entregados: Int = 0,
    val rechazados: Int = 0,
    val error: String? = null
)

class MiHuellaEcologicaViewModel(
    private val donationRepository: DonationFirestoreRepository,
    private val uid: String
) : ViewModel() {

    private val _state = MutableStateFlow(MiHuellaEcologicaUiState(isLoading = true))
    val state: StateFlow<MiHuellaEcologicaUiState> = _state.asStateFlow()

    init { calcularImpacto() }

    // (nuevo) Calcula impacto real desde el servidor (con respaldo a Firestore).
    private fun calcularImpacto() {
        viewModelScope.launch {
            _state.value = MiHuellaEcologicaUiState(isLoading = true)
            try {
                val todasDonaciones = donationRepository.obtenerPorUsuarioUnaVez(uid)

                // (nuevo) Desglose por estado.
                val pendientes = todasDonaciones.count { it.estadoTrazabilidad == "Pendiente" }
                val enRevision = todasDonaciones.count { it.estadoTrazabilidad == "En Revision" }
                val aprobados = todasDonaciones.count { it.estadoTrazabilidad == "Aprobado" }
                val entregados = todasDonaciones.count { it.estadoTrazabilidad == "Entregado" }
                val rechazados = todasDonaciones.count { it.estadoTrazabilidad == "Rechazado" }

                val validas = todasDonaciones.filter {
                    it.estadoTrazabilidad == "Aprobado" || it.estadoTrazabilidad == "Entregado"
                }
                val co2Total = validas.sumOf { it.co2AhorradoKg }

                println(">>> HUELLA: total=${todasDonaciones.size} validas=${validas.size} co2=$co2Total")

                _state.value = MiHuellaEcologicaUiState(
                    donacionesValidas = validas.size,
                    co2TotalKg = co2Total,
                    pendientes = pendientes,
                    enRevision = enRevision,
                    aprobados = aprobados,
                    entregados = entregados,
                    rechazados = rechazados
                )
            } catch (e: Exception) {
                _state.value = MiHuellaEcologicaUiState(error = "Error al calcular huella: ${e.message}")
            }
        }
    }

    fun recalcular() = calcularImpacto()
}

class MiHuellaEcologicaViewModelFactory(
    private val donationRepository: DonationFirestoreRepository,
    private val uid: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MiHuellaEcologicaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MiHuellaEcologicaViewModel(donationRepository, uid) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
