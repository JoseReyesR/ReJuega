// (nuevo) Archivo: PickupEventDao.kt
// (nuevo) Descripción: Data Access Object para la entidad PickupEvent.
// (nuevo) Define las consultas SQL sobre la tabla pickup_events.
package pe.rejuega.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// (nuevo) DAO para operaciones sobre eventos de recojo.
@Dao
interface PickupEventDao {

    // (nuevo) Obtiene solo los eventos de recojo activos (para mostrar en el mapa).
    // (nuevo) Retorna Flow para que la UI se actualice automáticamente (RN-03).
    @Query("SELECT * FROM pickup_events WHERE activo = 1")
    fun obtenerPickupEventsActivos(): Flow<List<PickupEvent>>

    // (nuevo) Obtiene los eventos de recojo de una campaña específica.
    // (nuevo) Se usará en el detalle de campaña para mostrar sus eventos de recojo.
    @Query("SELECT * FROM pickup_events WHERE campaignId = :campaignId AND activo = 1")
    fun obtenerPickupEventsPorCampaign(campaignId: Long): Flow<List<PickupEvent>>

    // (nuevo) Inserta un evento de recojo (lo usará el Admin en el futuro).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPickupEvent(pickupEvent: PickupEvent)

    // (nuevo) Inserta varios eventos de recojo de prueba (datos semilla).
    // (nuevo) IGNORE evita duplicados si ya existen con el mismo ID.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarPickupEventsSeed(pickupEvents: List<PickupEvent>)

    // (nuevo) Desactiva un evento de recojo (baja lógica).
    @Query("UPDATE pickup_events SET activo = 0 WHERE id = :id")
    suspend fun desactivarPickupEvent(id: Long)
}