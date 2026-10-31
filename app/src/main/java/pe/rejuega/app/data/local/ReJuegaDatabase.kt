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
    entities = [
        Campaign::class,
        Juguete::class,
        PuntoEntrega::class,
        MaterialEco::class,
        // (nuevo) Nueva entidad para eventos de recojo por campaña.
        PickupEvent::class],
    // (modificado) Versión 5:  se agregó la tabla pickup_events.

    version = 5, // (modificado) Subimos la versión a 5 porque cambió la estructura/nombres
    exportSchema = false
)
abstract class ReJuegaDatabase : RoomDatabase() {

    // (nuevo) Aquí declararemos los DAOs para poder usarlos en la app
    // (nuevo) Room generará el código automáticamente al compilar
    abstract fun campaignDao(): CampaignDao
    abstract fun jugueteDao(): JugueteDao
    abstract fun puntoEntregaDao(): PuntoEntregaDao
    abstract fun materialEcoDao(): MaterialEcoDao

    // (nuevo) DAO para eventos de recojo. Se agrega junto a los DAOs existentes.
    abstract fun pickupEventDao(): PickupEventDao
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