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
// (nuevo) Import necesario para el DTO de impacto.
import pe.rejuega.app.data.remote.ImpactoDonacionDto

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


    // (nuevo) Envía el impacto acumulado del usuario a MockAPI mediante POST.
    // (nuevo) Retorna un Result que encapsula el éxito o el fallo de la operación.
    suspend fun registrarImpactoAcumulado(
        uidUsuario: String,
        co2Total: Double,
        fecha: String
    ): Result<ImpactoDonacionDto> {
        return try {
            // (nuevo) Creamos el DTO con los datos del usuario.
            val impactoDto = ImpactoDonacionDto(
                uidUsuario = uidUsuario,
                co2TotalKg = co2Total,
                fechaRegistro = fecha
            )

            // (nuevo) Ejecutamos la llamada suspendida a la API.
            val respuesta = service.registrarImpacto(impactoDto)

            // (nuevo) Si llega aquí, la API respondió con éxito (código 200 o 201).
            Result.success(respuesta)
        } catch (e: IOException) {
            // (nuevo) Error de conectividad: sin internet, timeout, DNS, modo avión.
            // (nuevo) Lanzamos una excepción personalizada con mensaje amigable.
            Result.failure(Exception("Sin conexión a internet. Verifica tu red e intenta de nuevo."))
        } catch (e: HttpException) {
            // (nuevo) Error HTTP: el servidor respondió pero con un código de error (4xx, 5xx).
            // (nuevo) Ejemplo: 400 Bad Request, 500 Internal Server Error.
            Result.failure(Exception("Error del servidor (${e.code()}). Intenta más tarde."))
        } catch (e: Exception) {
            // (nuevo) Cualquier otro error inesperado (ej. error de parseo JSON).
            Result.failure(Exception("Error inesperado al registrar el impacto."))
        }
    }
}