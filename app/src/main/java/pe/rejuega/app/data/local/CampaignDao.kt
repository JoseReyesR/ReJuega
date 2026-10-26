// (nuevo) Archivo: CampaignDao.kt
// (nuevo) Descripción: Data Access Object para la entidad Campaign.
// (nuevo) Define las operaciones SQL que se pueden realizar sobre la tabla "campaigns".
package pe.rejuega.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// (nuevo) La anotación @Dao le indica a Room que esta interfaz contiene los métodos de acceso a datos
@Dao
interface CampaignDao {

    // (nuevo) Obtiene todas las campañas ordenadas por fecha de inicio (más recientes primero).
    // (nuevo) Retorna un Flow: Room emitirá automáticamente una nueva lista cada vez que los datos cambien.
    @Query("SELECT * FROM campaigns ORDER BY fechaInicio DESC")
    fun obtenerCampaigns(): Flow<List<Campaign>>

    // (nuevo) Obtiene una campaña específica por su ID (útil para la pantalla de detalle).
    @Query("SELECT * FROM campaigns WHERE id = :id")
    suspend fun obtenerCampaignPorId(id: Long): Campaign?

    // (nuevo) Inserta una nueva campaña. Si ya existe una con el mismo ID, la reemplaza.
    // (nuevo) La palabra 'suspend' indica que esta función usa corrutinas y no bloqueará la app.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCampaign(campaign: Campaign)

    // (nuevo) Inserta una lista completa de campañas (ideal para cargar los datos semilla).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCampaigns(campaigns: List<Campaign>)

    // (nuevo) Elimina una campaña específica.
    @Query("DELETE FROM campaigns WHERE id = :id")
    suspend fun eliminarCampaign(id: Long)
}