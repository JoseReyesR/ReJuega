// (nuevo) ViewModel para editar una donación en estado Pendiente.
package pe.rejuega.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.remote.Donacion
import pe.rejuega.app.data.remote.MaterialJugueteDto
import pe.rejuega.app.data.repository.DonationFirestoreRepository
import pe.rejuega.app.data.repository.MaterialEcoRepository

class EditarDonacionViewModel(
    private val donationRepository: DonationFirestoreRepository,
    private val materialEcoRepository: MaterialEcoRepository,
    private val donationId: String
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val nombre: String = "",
        val descripcion: String = "",
        val condicion: String = "Buen Estado",
        val materialTipo: String = "",
        val co2: Double = 0.0,
        val materiales: List<MaterialJugueteDto> = emptyList(),
        val mensaje: String? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val materiales = materialEcoRepository.obtenerMaterialesUnaVez()
                val don = donationRepository.obtenerPorIdUnaVez(donationId)
                _state.value = UiState(
                    isLoading = false,
                    nombre = don?.nombre ?: "",
                    descripcion = don?.descripcion ?: "",
                    condicion = don?.estado ?: "Buen Estado",
                    materialTipo = don?.materialTipo ?: "",
                    co2 = don?.co2AhorradoKg ?: 0.0,
                    materiales = materiales
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onNombre(v: String) { _state.value = _state.value.copy(nombre = v) }
    fun onDescripcion(v: String) { _state.value = _state.value.copy(descripcion = v) }
    fun onCondicion(v: String) { _state.value = _state.value.copy(condicion = v) }
    fun onMaterial(m: MaterialJugueteDto) {
        _state.value = _state.value.copy(materialTipo = m.tipoMaterial, co2 = m.co2AhorradoKg)
    }

    fun guardar(uid: String, campaignId: Long, campaignTitulo: String, fotoUrl: String) {
        viewModelScope.launch {
            val s = _state.value
            if (s.nombre.isBlank()) { _state.value = s.copy(error = "El nombre es obligatorio"); return@launch }
            try {
                val don = Donacion(
                    id = donationId, uid = uid, campaignId = campaignId,
                    campaignTitulo = campaignTitulo, nombre = s.nombre.trim(),
                    descripcion = s.descripcion.trim(), estado = s.condicion,
                    fotoUrl = fotoUrl, estadoTrazabilidad = "Pendiente",
                    materialTipo = s.materialTipo, co2AhorradoKg = s.co2,
                    fechaRegistro = System.currentTimeMillis()
                )
                donationRepository.actualizarDonacion(don)
                pe.rejuega.app.utils.RefreshTrigger.notificar()
                _state.value = _state.value.copy(mensaje = "Donación actualizada")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun limpiarMensaje() { _state.value = _state.value.copy(mensaje = null, error = null) }
}

class EditarDonacionViewModelFactory(
    private val donationRepository: DonationFirestoreRepository,
    private val materialEcoRepository: MaterialEcoRepository,
    private val donationId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditarDonacionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditarDonacionViewModel(donationRepository, materialEcoRepository, donationId) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
