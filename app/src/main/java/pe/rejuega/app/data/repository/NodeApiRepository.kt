// (nuevo) Repository que ejecuta pruebas contra el servidor y captura el código HTTP.
package pe.rejuega.app.data.repository

import pe.rejuega.app.data.remote.MaterialCreateRequest
import pe.rejuega.app.data.remote.NodeApiService
import pe.rejuega.app.domain.model.PruebaApi

class NodeApiRepository(
    private val service: NodeApiService
) {

    // (nuevo) GET /api/materiales → espera 200.
    suspend fun probarGetMateriales(): PruebaApi = try {
        val resp = service.obtenerMateriales()
        PruebaApi(
            nombre = "Listar materiales", metodo = "GET", url = "/api/materiales",
            codigo = resp.code(), exito = resp.isSuccessful,
            detalle = if (resp.isSuccessful) "${resp.body()?.count ?: 0} materiales recibidos"
                      else "Error del servidor"
        )
    } catch (e: Exception) {
        PruebaApi("Listar materiales", "GET", "/api/materiales", 0, false, "Sin conexión: ${e.message}")
    }

    // (nuevo) GET /api/materiales/1 → espera 200.
    suspend fun probarGetMaterialExistente(): PruebaApi = try {
        val resp = service.obtenerMaterialPorId(1)
        PruebaApi(
            nombre = "Material existente (id=1)", metodo = "GET", url = "/api/materiales/1",
            codigo = resp.code(), exito = resp.isSuccessful,
            detalle = if (resp.isSuccessful) resp.body()?.data?.tipoMaterial ?: "OK"
                      else resp.body()?.error ?: "Error"
        )
    } catch (e: Exception) {
        PruebaApi("Material existente", "GET", "/api/materiales/1", 0, false, "Sin conexión: ${e.message}")
    }

    // (nuevo) GET /api/materiales/9999 → espera 404.
    suspend fun probarGetMaterialInexistente(): PruebaApi = try {
        val resp = service.obtenerMaterialPorId(9999)
        PruebaApi(
            nombre = "Material inexistente (id=9999)", metodo = "GET", url = "/api/materiales/9999",
            codigo = resp.code(), exito = false,
            detalle = resp.body()?.error ?: "Recurso no encontrado"
        )
    } catch (e: Exception) {
        PruebaApi("Material inexistente", "GET", "/api/materiales/9999", 0, false, "Sin conexión: ${e.message}")
    }

    // (nuevo) POST válido → espera 201.
    suspend fun probarPostValido(): PruebaApi = try {
        val resp = service.crearMaterial(
            MaterialCreateRequest(
                tipoMaterial = "Prueba Android ${System.currentTimeMillis() % 1000}",
                co2AhorradoKg = 1.25,
                descripcionImpacto = "Material creado desde la app Android.",
                imagenUrl = null
            )
        )
        PruebaApi(
            nombre = "Crear material (body válido)", metodo = "POST", url = "/api/materiales",
            codigo = resp.code(), exito = resp.isSuccessful,
            detalle = resp.body()?.message ?: resp.body()?.error ?: ""
        )
    } catch (e: Exception) {
        PruebaApi("Crear material válido", "POST", "/api/materiales", 0, false, "Sin conexión: ${e.message}")
    }

    // (nuevo) POST inválido (falta co2) → espera 400.
    suspend fun probarPostInvalido(): PruebaApi = try {
        val resp = service.crearMaterial(
            MaterialCreateRequest(tipoMaterial = "Solo nombre sin CO2", co2AhorradoKg = -1.0)
        )
        PruebaApi(
            nombre = "Crear material (body inválido)", metodo = "POST", url = "/api/materiales",
            codigo = resp.code(), exito = false,
            detalle = resp.body()?.error ?: "Solicitud rechazada"
        )
    } catch (e: Exception) {
        PruebaApi("Crear material inválido", "POST", "/api/materiales", 0, false, "Sin conexión: ${e.message}")
    }

    // (nuevo) Ruta inexistente → espera 404 global.
    suspend fun probarRutaInexistente(): PruebaApi = try {
        val resp = service.rutaInexistente()
        PruebaApi(
            nombre = "Ruta inexistente", metodo = "GET", url = "/api/esto-no-existe",
            codigo = resp.code(), exito = false,
            detalle = "El servidor respondió 404 global"
        )
    } catch (e: Exception) {
        PruebaApi("Ruta inexistente", "GET", "/api/esto-no-existe", 0, false, "Sin conexión: ${e.message}")
    }
}
