// (nuevo) Archivo: PuntoEntregaDao.kt
// (nuevo) Descripción: Data Access Object para la entidad PuntoEntrega.
package pe.rejuega.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PuntoEntregaDao {

    // (nuevo) Obtiene solo los puntos de entrega activos para mostrar en el mapa
    @Query("SELECT * FROM puntos_entrega WHERE activo = 1")
    fun obtenerPuntosActivos(): Flow<List<PuntoEntrega>>

    // (nuevo) Inserta un nuevo punto de entrega (Solo rol Administrador - RB-08)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPuntoEntrega(punto: PuntoEntrega)

    // (nuevo) Desactiva un punto de entrega (baja lógica) sin eliminarlo de la base de datos
    @Query("UPDATE puntos_entrega SET activo = 0 WHERE id = :id")
    suspend fun desactivarPuntoEntrega(id: Long)

    // (nuevo) Inserta varios puntos de entrega de prueba (datos semilla).
    // (nuevo) IGNORE evita duplicados si ya existen con el mismo ID.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarPuntosEntregaSeed(puntosEntrega: List<PuntoEntrega>)
}