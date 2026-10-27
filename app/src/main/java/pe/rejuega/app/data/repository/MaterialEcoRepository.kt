// (nuevo) Archivo: MaterialEcoRepository.kt
// (nuevo) Descripción: Repositorio para el consumo de materiales ecológicos desde MockAPI.
// (nuevo) Responsabilidad: abstraer el acceso a la API REST y manejar errores de red.
package pe.rejuega.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.rejuega.app.data.remote.MaterialJugueteDto
import pe.rejuega.app.data.remote.RetrofitClient
import retrofit2.HttpException
import java.io.IOException

// (nuevo) Repositorio que consume el recurso materiales_juguetes de MockAPI.
class MaterialEcoRepository {

    // (nuevo) Instancia del servicio Retrofit (obtenida del cliente singleton).
    private val service = RetrofitClient.getMaterialJugueteService()

    // (nuevo) Obtiene la lista de materiales desde la API REST.
    // (nuevo) Retorna un Flow que emite el estado de la operación (éxito o error).
    // (nuevo) El ViewModel observa este Flow y actualiza la UI según el resultado.
    fun obtenerMateriales(): Flow<Result<List<MaterialJugueteDto>>> = flow {
        try {
            // (nuevo) Llamada suspendida a la API (no bloquea el hilo principal).
            val materiales = service.obtenerMateriales()

            // (nuevo) Si la llamada es exitosa, emitimos un Result.success con los datos.
            emit(Result.success(materiales))
        } catch (e: IOException) {
            // (nuevo) Error de red (sin internet, timeout, DNS, etc.).
            // (nuevo) Emitimos un Result.failure con un mensaje amigable.
            emit(Result.failure(e))
        } catch (e: HttpException) {
            // (nuevo) Error HTTP (404 Not Found, 500 Internal Server Error, etc.).
            // (nuevo) Emitimos un Result.failure con el código de error.
            emit(Result.failure(e))
        } catch (e: Exception) {
            // (nuevo) Cualquier otro error inesperado.
            emit(Result.failure(e))
        }
    }

    // (nuevo) Obtiene un material específico por su ID.
    // (nuevo) Útil para mostrar el impacto de un juguete en particular.
    suspend fun obtenerMaterialPorId(id: String): Result<MaterialJugueteDto> {
        return try {
            val material = service.obtenerMaterialPorId(id)
            Result.success(material)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}