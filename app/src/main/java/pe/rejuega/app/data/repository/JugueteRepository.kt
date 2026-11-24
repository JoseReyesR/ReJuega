// (modificado) Archivo: JugueteRepository.kt
// (modificado) Descripción: Repositorio de juguetes con lógica de trazabilidad.
// (modificado) Cambio: Se agrega flujo de todos los juguetes y avance secuencial de estados.
package pe.rejuega.app.data.repository

import kotlinx.coroutines.flow.Flow
import pe.rejuega.app.data.local.Juguete
import pe.rejuega.app.data.local.JugueteDao
import pe.rejuega.app.utils.DatosSemilla
import pe.rejuega.app.utils.EstadosTrazabilidad

// (modificado) Repositorio: única fuente de verdad para el ViewModel de juguetes.
class JugueteRepository(
    private val jugueteDao: JugueteDao
) {

    // (nuevo) Flujo de TODOS los juguetes registrados localmente.
    // (nuevo) Room reemitirá la lista automáticamente cuando se inserte o actualice un juguete.
    val juguetes: Flow<List<Juguete>> = jugueteDao.obtenerJuguetes()

    // (modificado) Inserta una propuesta de juguete creada por el donante.
    suspend fun insertarJuguete(juguete: Juguete) {
        jugueteDao.insertarJuguete(juguete)
    }

    // (modificado) Obtiene los juguetes de una campaña específica (para el detalle).
    fun obtenerJuguetesPorCampaign(campaignId: Long): Flow<List<Juguete>> {
        return jugueteDao.obtenerJuguetesPorCampaign(campaignId)
    }

    // (modificado) Carga los datos hardcodeados de prueba.
    suspend fun inicializarDatosSemilla() {
        try {
            jugueteDao.insertarJuguetesSeed(DatosSemilla.listaJuguetes)
        } catch (e: Exception) {
            // (modificado) Se registra el error sin detener el arranque de la app.
            println("Error al insertar juguetes semilla: ${e.message}")
        }
    }

    // (nuevo) Avanza un juguete al siguiente estado permitido (RB-02).
    // (nuevo) Solo debe invocarse desde un contexto con rol Administrador.
    // (nuevo) Lanza excepciones con mensajes amigables si la operación no es válida.
    suspend fun avanzarEstadoJuguete(id: Long) {

        // (nuevo) 1. Leemos el estado real desde la BD (nunca confiamos solo en la UI).
        val juguete = jugueteDao.obtenerJuguetePorId(id)
            ?: throw IllegalStateException("No se encontró el juguete seleccionado.")

        // (nuevo) 2. Calculamos el siguiente estado siguiendo el orden oficial.
        val siguienteEstado = EstadosTrazabilidad.siguienteDe(juguete.estadoTrazabilidad)

        // (nuevo) 3. Si es null, el juguete ya está Entregado y no se puede modificar.
        if (siguienteEstado == null) {
            throw IllegalStateException("Este juguete ya fue entregado. El proceso ha finalizado.")
        }

        // (nuevo) 4. Ejecutamos la actualización usando corrutinas (no bloquea la UI).
        jugueteDao.actualizarEstadoTrazabilidad(id, siguienteEstado)
    }
}