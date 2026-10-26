// (nuevo) Archivo: MaterialEcoDao.kt
// (nuevo) Descripción: Data Access Object para la entidad MaterialEco.
// (nuevo) Esta entidad servirá como caché local de los datos que luego vendrán de MockAPI.io.
package pe.rejuega.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialEcoDao {

    // (nuevo) Obtiene todos los materiales ecológicos disponibles
    @Query("SELECT * FROM materiales_eco")
    fun obtenerMateriales(): Flow<List<MaterialEco>>

    // (nuevo) Inserta o actualiza la lista completa de materiales desde la API REST
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMateriales(materiales: List<MaterialEco>)
}