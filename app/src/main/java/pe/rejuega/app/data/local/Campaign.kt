
//  Descripción: Entidad que representa una campaña de donación.
//  Motivo: Se usa el nombre "Campaign" ÚNICAMENTE porque la palabra "Campaña" contiene la letra 'ñ'.
package pe.rejuega.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// (modificado) El nombre de la tabla en la base de datos será "campaigns"
@Entity(tableName = "campaigns")
data class Campaign(
    // (modificado) ID autogenerado por Room
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // (nuevo) ID del documento en Firestore. Permite mapear entre Room y Firestore.
    val firestoreId: String = "",
    // (modificado) Campos en español sin tildes para evitar caracteres especiales
    val titulo: String,
    val descripcion: String,
    val fechaInicio: String, // (modificado) Formato sugerido: YYYY-MM-DD
    val fechaFin: String,
    val estado: String,      // (modificado) Ejemplos: "Activa", "Cerrada"
    val imagenUrl: String,
    // (nuevo) Timestamp de la última sincronización exitosa.
    val ultimaSincronizacion: Long = 0
)