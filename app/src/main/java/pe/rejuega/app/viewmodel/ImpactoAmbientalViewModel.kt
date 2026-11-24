// (nuevo) ViewModel de Impacto Ambiental: lista los materiales y su CO₂ (como antes).
package pe.rejuega.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.remote.MaterialJugueteDto
import pe.rejuega.app.data.repository.MaterialEcoRepository

class ImpactoAmbientalViewModel(
    private val materialEcoRepository: MaterialEcoRepository
) : ViewModel() {

    private val _materiales = MutableStateFlow<List<MaterialJugueteDto>>(emptyList())
    val materiales: StateFlow<List<MaterialJugueteDto>> = _materiales.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init { cargarMateriales() }

    private fun cargarMateriales() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _materiales.value = materialEcoRepository.obtenerMaterialesUnaVez()
            } catch (e: Exception) {
                _materiales.value = emptyList()
            }
            _isLoading.value = false
        }
    }
}

class ImpactoAmbientalViewModelFactory(
    private val materialEcoRepository: MaterialEcoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImpactoAmbientalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImpactoAmbientalViewModel(materialEcoRepository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
