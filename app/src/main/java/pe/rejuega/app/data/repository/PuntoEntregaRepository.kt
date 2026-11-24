// (nuevo) Archivo: PuntoEntregaRepository.kt
// (nuevo) Descripción: Repositorio para los puntos de entrega fijos de la ONG.
// (nuevo) Responsabilidad: exponer los puntos fijos desde Room al ViewModel.
package pe.rejuega.app.data.repository

import kotlinx.coroutines.flow.Flow
import pe.rejuega.app.data.local.PuntoEntrega
import pe.rejuega.app.data.local.PuntoEntregaDao
import pe.rejuega.app.utils.DatosSemilla

// (nuevo) Repositorio que gestiona los puntos de entrega fijos.
class PuntoEntregaRepository(
    private val puntoEntregaDao: PuntoEntregaDao
) {

    // (nuevo) Obtiene los puntos de entrega activos como Flow.
    // (nuevo) Se usará en el mapa para mostrar los marcadores (futuro).
    fun obtenerPuntosActivos(): Flow<List<PuntoEntrega>> {
        return puntoEntregaDao.obtenerPuntosActivos()
    }

    // (nuevo) Inserta los datos semilla de puntos de entrega.
    suspend fun inicializarDatosSemilla() {
        try {
            puntoEntregaDao.insertarPuntosEntregaSeed(DatosSemilla.listaPuntosEntrega)
        } catch (e: Exception) {
            println("Error al insertar puntos de entrega semilla: ${e.message}")
        }
    }
}