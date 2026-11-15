// (nuevo) ViewModel del mapa: unifica puntos fijos y eventos activos,
// (nuevo) y calcula el punto recomendado por cercanía (HU-10).
package pe.rejuega.app.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.repository.PuntoRecojoFirestoreRepository
import pe.rejuega.app.domain.model.PuntoMapa

class MapaViewModel(
    private val repository: PuntoRecojoFirestoreRepository
) : ViewModel() {

    private val _puntos = MutableStateFlow<List<PuntoMapa>>(emptyList())
    val puntos: StateFlow<List<PuntoMapa>> = _puntos.asStateFlow()

    // (nuevo) Punto recomendado por cercanía (null si no hay ubicación).
    private val _recomendado = MutableStateFlow<PuntoMapa?>(null)
    val recomendado: StateFlow<PuntoMapa?> = _recomendado.asStateFlow()

    // (nuevo) Distancia en metros al punto recomendado.
    private val _distanciaMetros = MutableStateFlow<Float>(0f)
    val distanciaMetros: StateFlow<Float> = _distanciaMetros.asStateFlow()

    init { observar() }

    private fun observar() {
        viewModelScope.launch {
            try {
                repository.observarPuntosActivos().collect { puntosFijos ->
                    val fijos = puntosFijos.map {
                        PuntoMapa(
                            id = it.id, tipo = "Punto fijo", nombre = it.nombre,
                            direccion = it.direccion, detalle = it.horario,
                            latitud = it.latitud, longitud = it.longitud
                        )
                    }
                    _puntos.value = fijos + _puntos.value.filter { it.tipo == "Evento de recojo" }
                }
            } catch (e: Exception) {
                println("Error observando puntos activos: ${e.message}")
            }
        }
        viewModelScope.launch {
            try {
                repository.observarEventos().collect { eventos ->
                    val activos = eventos.filter { it.activo }.map {
                        PuntoMapa(
                            id = it.id, tipo = "Evento de recojo", nombre = it.nombre,
                            direccion = "${it.lugar} - ${it.direccion}",
                            detalle = "${it.fecha} a las ${it.hora}",
                            latitud = it.latitud, longitud = it.longitud,
                            campaignTitulo = it.campaignTitulo
                        )
                    }
                    _puntos.value = _puntos.value.filter { it.tipo == "Punto fijo" } + activos
                }
            } catch (e: Exception) {
                println("Error observando eventos activos: ${e.message}")
            }
        }
    }

    // (nuevo) Recibe la ubicación del usuario (una sola lectura) y recomienda el más cercano.
    fun calcularRecomendado(latUsuario: Double, lonUsuario: Double) {
        val resultados = FloatArray(1)
        var mejor: PuntoMapa? = null
        var mejorDistancia = Float.MAX_VALUE
        for (p in _puntos.value) {
            Location.distanceBetween(latUsuario, lonUsuario, p.latitud, p.longitud, resultados)
            if (resultados[0] < mejorDistancia) {
                mejorDistancia = resultados[0]
                mejor = p
            }
        }
        _recomendado.value = mejor
        _distanciaMetros.value = mejorDistancia
    }

    fun limpiarRecomendado() {
        _recomendado.value = null
    }
}

class MapaViewModelFactory(
    private val repository: PuntoRecojoFirestoreRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapaViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
