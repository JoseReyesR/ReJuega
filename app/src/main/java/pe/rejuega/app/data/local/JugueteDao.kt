// (modificado) Archivo: JugueteDao.kt
// (modificado) Descripción: Data Access Object para la entidad Juguete.
// (modificado) Cambio: Se agrega método para insertar una lista de juguetes semilla.
package pe.rejuega.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// (modificado) DAO para operaciones sobre la tabla juguetes.
@Dao
interface JugueteDao {

    // (modificado) Obtiene todos los juguetes propuestos para donación.
    @Query("SELECT * FROM juguetes ORDER BY id DESC")
    fun obtenerJuguetes(): Flow<List<Juguete>>

    // (modificado) Obtiene los juguetes asociados a una campaña específica.
    @Query("SELECT * FROM juguetes WHERE campaignId = :campaignId")
    fun obtenerJuguetesPorCampaign(campaignId: Long): Flow<List<Juguete>>

    // (modificado) Actualiza el estado de trazabilidad de un juguete.
    // (modificado) Regla RB-02: Solo el Administrador podrá ejecutar esto cuando exista login por roles.
    @Query("UPDATE juguetes SET estadoTrazabilidad = :nuevoEstado WHERE id = :id")
    suspend fun actualizarEstadoTrazabilidad(id: Long, nuevoEstado: String)

    // (modificado) Inserta una nueva propuesta de juguete.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarJuguete(juguete: Juguete)

    // (nuevo) Inserta una lista de juguetes de prueba.
    // (nuevo) Se usa IGNORE para no sobrescribir juguetes si ya existen con el mismo ID.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarJuguetesSeed(juguetes: List<Juguete>)
}