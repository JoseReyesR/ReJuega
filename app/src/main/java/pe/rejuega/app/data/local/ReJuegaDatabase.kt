// (modificado) Archivo: ReJuegaDatabase.kt
// (modificado) Descripción: Configuración central de la base de datos Room.
// (modificado) Motivo: Actualización de la lista de entidades (Español + Campaign).
package pe.rejuega.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// (modificado) Se listan las entidades con sus nombres finales
@Database(
    entities = [Campaign::class, Juguete::class, PuntoEntrega::class, MaterialEco::class],
    // (modificado) Versión 4: se actualizaron los datos semilla de trazabilidad.
// (modificado) Atencion: borrará los juguetes registrados manualmente en el emulador.
// (modificado) En producción se usará una migración real con Migrations().
    version = 4, // (modificado) Subimos la versión a 3 porque cambió la estructura/nombres
    exportSchema = false
)
abstract class ReJuegaDatabase : RoomDatabase() {

    // (nuevo) Aquí declararemos los DAOs para poder usarlos en la app
    // (nuevo) Room generará el código automáticamente al compilar
    abstract fun campaignDao(): CampaignDao
    abstract fun jugueteDao(): JugueteDao
    abstract fun puntoEntregaDao(): PuntoEntregaDao
    abstract fun materialEcoDao(): MaterialEcoDao

    companion object {
        @Volatile
        private var INSTANCE: ReJuegaDatabase? = null

        fun getDatabase(context: Context): ReJuegaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReJuegaDatabase::class.java,
                    "rejuega_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}