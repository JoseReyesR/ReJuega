// (nuevo) Archivo: DonationRepository.kt
// (nuevo) Descripción: Repositorio que orquesta el registro completo de una donación.
// (nuevo) Flujo: Comprimir imagen → Subir a Cloudinary → Guardar en Firestore → Guardar en Room.
// (nuevo) Usa Cloudinary en lugar de Firebase Storage para evitar costos.
package pe.rejuega.app.data.remote

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import pe.rejuega.app.data.local.Juguete
import pe.rejuega.app.data.local.JugueteDao
import pe.rejuega.app.utils.ImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// (nuevo) Data class que representa el documento de donación en Firestore.
// (nuevo) Firestore mapea automáticamente estas propiedades a campos del documento.
// (nuevo) Los valores por defecto son necesarios para que Firestore pueda deserializar.
data class Donacion(
    val uid: String = "",
    val campaignId: Long = 0,
    val nombre: String = "",
    val descripcion: String = "",
    val estado: String = "",
    val fotoUrl: String = "",
    val estadoTrazabilidad: String = "Pendiente",
    val fechaRegistro: Long = 0
)

// (nuevo) Repositorio que gestiona el registro completo de donaciones.
// (nuevo) Recibe tres dependencias:
// (nuevo)   firestore    - Para guardar la donación en la nube.
// (nuevo)   jugueteDao   - Para guardar la donación localmente (Room) y verla en trazabilidad.
class DonationRepository(
    private val firestore: FirebaseFirestore,
    private val jugueteDao: JugueteDao
) {

    // =====================================================================
    // (nuevo) CONFIGURACIÓN DE CLOUDINARY
    // =====================================================================
    // (nuevo) IMPORTANTE: Reemplaza "TU_CLOUD_NAME" con tu Cloud Name real.
    // (nuevo) Lo obtienes del Dashboard de Cloudinary (subpaso 10.1.2).
    // (nuevo) El upload preset debe coincidir con el que creaste en Cloudinary.
    // =====================================================================
    private companion object {
        const val CLOUD_NAME = "eg8sugwt"          // nombre del cloud name:
        const val UPLOAD_PRESET = "rejuega_preset"      // nombre del Upload Preset:
    }

    // (nuevo) Servicio de Cloudinary obtenido del RetrofitClient.
    private val cloudinaryService = RetrofitClient.getCloudinaryService()

    // =====================================================================
    // (nuevo) MÉTODO PRINCIPAL: REGISTRAR PROPUESTA DE DONACIÓN
    // =====================================================================
    // (nuevo) Este método hace TODO el flujo completo:
    // (nuevo)   1. Comprime la imagen (RB-09).
    // (nuevo)   2. Sube la imagen comprimida a Cloudinary.
    // (nuevo)   3. Guarda la donación en Firestore con la URL de la foto.
    // (nuevo)   4. Guarda la donación en Room para trazabilidad local.
    // (nuevo) Lanza excepciones si algo falla, para que el ViewModel las capture.
    // =====================================================================
    suspend fun registrarPropuesta(
        context: Context,
        uid: String,
        campaignId: Long,
        nombre: String,
        descripcion: String,
        estado: String,
        fotoUri: Uri
    ): String {
        // (nuevo) VALIDACIÓN PREVIA: verificar que exista un usuario autenticado.
        // (nuevo) Motivo: las reglas de Firestore exigen que el campo uid coincida
        //  con request.auth.uid. Si el uid viene vacio, la subida a Cloudinary
        //  se haria en vano y luego Firestore rechazaria el documento.

        if (uid.isBlank()) {
            throw IOException("No hay un usuario autenticado. Cierra sesión y vuelve a iniciar sesión.")
        }
        // (nuevo) PASO 1: Comprimir la imagen localmente (RB-09).
        // (nuevo) Si retorna null, significa que la imagen no se pudo procesar.
        val imageBytes = ImageUtils.compressImage(context, fotoUri)
            ?: throw IOException("No se pudo procesar ni comprimir la imagen seleccionada.")

        // (nuevo) PASO 2: Convertir los bytes comprimidos a un archivo temporal.
        // (nuevo) Retrofit necesita un File para construir el MultipartBody.
        val tempFile = File.createTempFile(
            "rejuega_upload_",  // (nuevo) Prefijo
            ".jpg",             // (nuevo) Extensión
            context.cacheDir    // (nuevo) Directorio de caché
        )

        // (nuevo) Escribir los bytes en el archivo temporal.
        // (nuevo) .use {} cierra el FileOutputStream automáticamente.
        FileOutputStream(tempFile).use { it.write(imageBytes) }

        // (nuevo) PASO 3: Construir el MultipartBody.Part para Retrofit.
        // (nuevo) Esto es lo que Retrofit enviará como cuerpo de la petición POST.
        val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData(
            "file",         // (nuevo) Nombre del campo en el formulario multipart
            tempFile.name,  // (nuevo) Nombre del archivo
            requestFile     // (nuevo) Contenido del archivo
        )

        // (nuevo) PASO 4: Crear el RequestBody para el upload preset.
        // (nuevo) Cloudinary necesita saber qué preset usar para validar la subida.
        val presetBody = UPLOAD_PRESET.toRequestBody("text/plain".toMediaTypeOrNull())

        // (nuevo) PASO 5: Subir la imagen a Cloudinary.
        // (nuevo) Esta llamada suspende la corrutina hasta que Cloudinary responda.
        // (nuevo) Si falla (sin internet, preset inválido), lanza una excepción.
        val cloudinaryResponse = cloudinaryService.uploadImage(
            cloudName = CLOUD_NAME,
            image = imagePart,
            uploadPreset = presetBody
        )

        // (nuevo) PASO 6: Obtener la URL pública de la imagen subida.
        // (nuevo) Esta URL es la que guardaremos en Firestore y Room.
        val downloadUrl = cloudinaryResponse.secure_url

        // (nuevo) PASO 7: Eliminar el archivo temporal para liberar espacio.
        // (nuevo) Ya no lo necesitamos porque la imagen está en Cloudinary.
        tempFile.delete()

        // (nuevo) PASO 8: Crear el objeto Donacion con todos los datos.
        // (nuevo) Este objeto se guardará en Firestore.
        val donacion = Donacion(
            uid = uid,
            campaignId = campaignId,
            nombre = nombre.trim(),
            descripcion = descripcion.trim(),
            estado = estado,
            fotoUrl = downloadUrl,
            estadoTrazabilidad = "Pendiente",
            fechaRegistro = System.currentTimeMillis()
        )

        // (nuevo) PASO 9: Guardar la donación en Firestore.
        // (nuevo) .add() genera un ID automático para el documento.
        // (nuevo) .await() suspende la corrutina hasta que Firestore confirme.
        firestore.collection("donations").add(donacion).await()

        // (nuevo) PASO 10: Guardar la donación en Room (base de datos local).
        // (nuevo) Esto permite que el usuario vea la donación en su trazabilidad
        // (nuevo) incluso si no hay internet (RB-05).
        val jugueteLocal = Juguete(
            campaignId = campaignId,
            nombre = nombre.trim(),
            descripcion = descripcion.trim(),
            estado = estado,
            fotoUrl = downloadUrl,
            estadoTrazabilidad = "Pendiente"
        )
        jugueteDao.insertarJuguete(jugueteLocal)

        // (nuevo) Retornar la URL de Cloudinary por si el ViewModel la necesita.
        return downloadUrl
    }
}