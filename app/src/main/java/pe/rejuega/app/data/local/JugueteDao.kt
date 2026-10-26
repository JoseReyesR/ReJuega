// (nuevo) Archivo: JugueteDao.kt
// (nuevo) Descripción: Data Access Object para la entidad Juguete.
package pe.rejuega.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JugueteDao {

    // (nuevo) Obtiene todos los juguetes propuestos para donación
    @Query("SELECT * FROM juguetes ORDER BY id DESC")
    fun obtenerJuguetes(): Flow<List<Juguete>>

    // (nuevo) Obtiene los juguetes asociados a una campaña específica
    @Query("SELECT * FROM juguetes WHERE campaignId = :campaignId")
    fun obtenerJuguetesPorCampaign(campaignId: Long): Flow<List<Juguete>>

    // (nuevo) Actualiza el estado de trazabilidad de un juguete (Regla de negocio RB-02)
    // (nuevo) Solo debería ser ejecutado por la lógica del Administrador
    @Query("UPDATE juguetes SET estadoTrazabilidad = :nuevoEstado WHERE id = :id")
    suspend fun actualizarEstadoTrazabilidad(id: Long, nuevoEstado: String)

    // (nuevo) Inserta una nueva propuesta de juguete
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarJuguete(juguete: Juguete)
}