// (nuevo) Archivo: PerfilViewModel.kt
// (nuevo) Descripción: ViewModel para la pantalla de Perfil e Impacto Acumulado (HU-12).
// (nuevo) Responsabilidad: Calcular el impacto local y enviarlo a MockAPI mediante POST.
package pe.rejuega.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.rejuega.app.data.repository.JugueteRepository
import pe.rejuega.app.data.repository.MaterialEcoRepository
import pe.rejuega.app.utils.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// (nuevo) Estado de la interfaz de la pantalla de Perfil.
data class PerfilUiState(
    val co2AcumuladoLocal: Double = 0.0,
    val cantidadJuguetes: Int = 0,
    val estadoPost: UiState<String> = UiState.Empty,
    val uidUsuario: String = ""
)

// (nuevo) ViewModel que gestiona el perfil del usuario y el registro de impacto.
class PerfilViewModel(
    private val jugueteRepository: JugueteRepository,
    private val materialEcoRepository: MaterialEcoRepository,
    private val uidUsuario: String
) : ViewModel() {

    // (nuevo) Estado interno de la UI.
    private val _uiState = MutableStateFlow(PerfilUiState(uidUsuario = uidUsuario))

    // (nuevo) Estado público de solo lectura.
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    // (nuevo) Al crear el ViewModel, calculamos el impacto local.
    init {
        calcularImpactoLocal()
    }

    // (nuevo) Calcula el CO2 ahorrado basándose en los juguetes registrados localmente en Room.
    // (nuevo) Para simplificar, asumimos un promedio de 2.5 kg de CO2 por cada juguete donado.
    // (nuevo) En una app real, esto se cruzaría con la tabla de MaterialesEco.
    private fun calcularImpactoLocal() {
        viewModelScope.launch {
            jugueteRepository.juguetes.collect { juguetes ->
                val cantidad = juguetes.size
                // (nuevo) Cálculo simulado: 2.5 kg por juguete.
                val co2Total = cantidad * 2.5

                _uiState.update {
                    it.copy(
                        cantidadJuguetes = cantidad,
                        co2AcumuladoLocal = co2Total
                    )
                }
            }
        }
    }

    // (nuevo) Envía el impacto acumulado a MockAPI mediante POST.
    fun sincronizarImpacto() {
        viewModelScope.launch {
            // (nuevo) Cambiamos el estado del POST a Loading.
            _uiState.update {
                it.copy(estadoPost = UiState.Loading)
            }

            // (nuevo) Generamos la fecha actual para el registro.
            val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            // (nuevo) Llamamos al repositorio para hacer el POST.
            val resultado = materialEcoRepository.registrarImpactoAcumulado(
                uidUsuario = _uiState.value.uidUsuario,
                co2Total = _uiState.value.co2AcumuladoLocal,
                fecha = fechaActual
            )

            // (nuevo) Procesamos el resultado (éxito o fallo).
            resultado.onSuccess {
                _uiState.update {
                    it.copy(estadoPost = UiState.Success("¡Impacto registrado exitosamente en la nube!"))
                }
            }.onFailure { error ->
                // (nuevo) Si falla, mostramos el mensaje de error (que ya viene amigable desde el Repository).
                _uiState.update {
                    it.copy(estadoPost = UiState.Error(error.message ?: "Error desconocido"))
                }
            }
        }
    }

    // (nuevo) Limpia el estado del POST para permitir reintentar o cerrar el mensaje.
    fun limpiarEstadoPost() {
        _uiState.update {
            it.copy(estadoPost = UiState.Empty)
        }
    }
}

// (nuevo) Factory para inyectar los repositorios y el UID del usuario.
class PerfilViewModelFactory(
    private val jugueteRepository: JugueteRepository,
    private val materialEcoRepository: MaterialEcoRepository,
    private val uidUsuario: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PerfilViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PerfilViewModel(jugueteRepository, materialEcoRepository, uidUsuario) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}