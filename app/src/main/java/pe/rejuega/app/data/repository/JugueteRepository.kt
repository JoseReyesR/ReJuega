// (nuevo) Archivo: JugueteRepository.kt
// (nuevo) Descripción: Repositorio para la entidad Juguete.
// (nuevo) Responsabilidad: Separar el acceso a datos de la capa UI y ViewModel.
package pe.rejuega.app.data.repository

import pe.rejuega.app.data.local.Juguete
import pe.rejuega.app.data.local.JugueteDao
import pe.rejuega.app.utils.DatosSemilla

// (nuevo) Repositorio de juguetes.
// (nuevo) Recibe el DAO mediante constructor para aplicar inyección de dependencias simple.
class JugueteRepository(
    private val jugueteDao: JugueteDao
) {

    // (nuevo) Inserta un juguete registrado por el usuario.
    // (nuevo) En Sprint 5, este juguete también se sincronizará con Firestore.
    suspend fun insertarJuguete(juguete: Juguete) {
        jugueteDao.insertarJuguete(juguete)
    }

    // (nuevo) Obtiene los juguetes de una campaña específica.
    // (nuevo) Retorna Flow para que la UI se actualice automáticamente si cambian los datos.
    fun obtenerJuguetesPorCampaign(campaignId: Long) =
        jugueteDao.obtenerJuguetesPorCampaign(campaignId)

    // (nuevo) Inserta juguetes hardcodeados de prueba.
    // (nuevo) Útil para que la app no inicie vacía durante el desarrollo académico.
    suspend fun inicializarDatosSemilla() {
        try {
            jugueteDao.insertarJuguetesSeed(DatosSemilla.listaJuguetes)
        } catch (e: Exception) {
            // (nuevo) Si ocurre un error al insertar datos semilla, se registra en consola.
            println("Error al insertar juguetes semilla: ${e.message}")
        }
    }
}