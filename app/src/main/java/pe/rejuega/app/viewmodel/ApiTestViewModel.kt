// (nuevo) ViewModel de la pantalla de pruebas del servidor (muestra códigos HTTP).
package pe.rejuega.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.repository.NodeApiRepository
import pe.rejuega.app.domain.model.PruebaApi

class ApiTestViewModel(
    private val repository: NodeApiRepository
) : ViewModel() {

    private val _pruebas = MutableStateFlow<List<PruebaApi>>(emptyList())
    val pruebas: StateFlow<List<PruebaApi>> = _pruebas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // (nuevo) Ejecuta una prueba y la agrega al inicio de la lista.
    private fun ejecutar(bloque: suspend () -> PruebaApi) {
        viewModelScope.launch {
            _isLoading.value = true
            val resultado = bloque()
            _pruebas.value = listOf(resultado) + _pruebas.value
            _isLoading.value = false
        }
    }

    fun probarGet200() = ejecutar { repository.probarGetMateriales() }
    fun probarGet200Uno() = ejecutar { repository.probarGetMaterialExistente() }
    fun probarGet404() = ejecutar { repository.probarGetMaterialInexistente() }
    fun probarPost201() = ejecutar { repository.probarPostValido() }
    fun probarPost400() = ejecutar { repository.probarPostInvalido() }
    fun probarRuta404() = ejecutar { repository.probarRutaInexistente() }

    // (nuevo) Ejecuta las 6 pruebas en secuencia (demo rápida).
    fun ejecutarTodas() {
        viewModelScope.launch {
            _isLoading.value = true
            val resultados = listOf(
                repository.probarGetMateriales(),
                repository.probarGetMaterialExistente(),
                repository.probarGetMaterialInexistente(),
                repository.probarPostValido(),
                repository.probarPostInvalido(),
                repository.probarRutaInexistente()
            )
            _pruebas.value = resultados + _pruebas.value
            _isLoading.value = false
        }
    }

    fun limpiar() { _pruebas.value = emptyList() }
}

class ApiTestViewModelFactory(
    private val repository: NodeApiRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ApiTestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ApiTestViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
