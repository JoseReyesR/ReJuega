// (nuevo) Archivo: ImpactoAmbientalViewModel.kt
// (nuevo) Descripción: ViewModel para la pantalla de impacto ambiental (HU-11).
// (nuevo) Responsabilidad: consumir MockAPI y exponer el estado a la UI.
package pe.rejuega.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.remote.MaterialJugueteDto
import pe.rejuega.app.data.repository.MaterialEcoRepository
import pe.rejuega.app.utils.UiState
import retrofit2.HttpException
import java.io.IOException

// (nuevo) ViewModel que gestiona la carga de datos de impacto ambiental desde MockAPI.
class ImpactoAmbientalViewModel(
    private val repository: MaterialEcoRepository
) : ViewModel() {

    // (nuevo) Estado interno de la UI. Comienza en Loading.
    private val _uiState = MutableStateFlow<UiState<List<MaterialJugueteDto>>>(UiState.Loading)

    // (nuevo) Estado público de solo lectura para la pantalla.
    val uiState: StateFlow<UiState<List<MaterialJugueteDto>>> = _uiState.asStateFlow()

    // (nuevo) Al crear el ViewModel, cargamos los materiales automáticamente.
    init {
        cargarMateriales()
    }

    // (nuevo) Función que consume la API REST y actualiza el estado de la UI.
    fun cargarMateriales() {
        viewModelScope.launch {
            // (nuevo) Indicamos que estamos cargando.
            _uiState.value = UiState.Loading

            // (nuevo) Observamos el Flow del repositorio.
            repository.obtenerMateriales().collect { result ->
                result.onSuccess { materiales ->
                    // (nuevo) Si la lista está vacía, mostramos estado Empty.
                    if (materiales.isEmpty()) {
                        _uiState.value = UiState.Empty
                    } else {
                        // (nuevo) Si hay datos, mostramos Success.
                        _uiState.value = UiState.Success(materiales)
                    }
                }.onFailure { error ->
                    // (nuevo) Si hay error, mostramos un mensaje amigable según el tipo.
                    val mensaje = when (error) {
                        is IOException -> "Sin conexión a internet. Verifica tu red."
                        is HttpException -> "Error del servidor: ${error.code()}"
                        else -> "Error inesperado: ${error.message}"
                    }
                    _uiState.value = UiState.Error(mensaje)
                }
            }
        }
    }
}

// (nuevo) Factory para inyectar el repositorio al ViewModel.
class ImpactoAmbientalViewModelFactory(
    private val repository: MaterialEcoRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImpactoAmbientalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImpactoAmbientalViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}