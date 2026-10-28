// (nuevo) Archivo: CloudinaryService.kt
// (nuevo) Descripción: Interfaz Retrofit para subir imágenes a Cloudinary.
// (nuevo) Usa el modo "unsigned upload" que no requiere API Secret en la app.
package pe.rejuega.app.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

// (nuevo) Data class que mapea la respuesta JSON de Cloudinary.
// (nuevo) Cloudinary responde con muchos campos, pero solo mapeamos los que necesitamos.
// (nuevo) Gson ignora automáticamente los campos que no están definidos aquí.
data class CloudinaryUploadResponse(
    // (nuevo) URL pública HTTPS de la imagen subida. Esta es la que guardamos en Firestore.
    val secure_url: String,

    // (nuevo) Identificador único de la imagen en Cloudinary.
    val public_id: String,

    // (nuevo) Ancho de la imagen en píxeles (para verificar compresión).
    val width: Int,

    // (nuevo) Alto de la imagen en píxeles.
    val height: Int,

    // (nuevo) Tamaño final del archivo en bytes.
    // (nuevo) Lo usamos para comprobar que la compresión RB-09 funcionó.
    val bytes: Long
)

// (nuevo) Interfaz Retrofit para la API de Cloudinary.
// (nuevo) La URL base se configura en RetrofitClient (subpaso 10.6).
interface CloudinaryService {

    // (nuevo) Sube una imagen al cloud especificado usando un upload preset unsigned.
    // (nuevo) Anotaciones:
    // (nuevo)   @Multipart  - La petición contiene datos binarios (la imagen).
    // (nuevo)   @POST       - Método HTTP POST.
    // (nuevo)   @Path       - Reemplaza {cloud_name} con el valor real.
    // (nuevo)   @Part       - Cada campo del formulario multipart.
    @Multipart
    @POST("v1_1/{cloud_name}/image/upload")
    suspend fun uploadImage(
        // (nuevo) Nombre del cloud. Se reemplaza en la URL.
        // (nuevo) Ejemplo: si cloud_name es "dabc123xyz", la URL será:
        // (nuevo) https://api.cloudinary.com/v1_1/dabc123xyz/image/upload
        @Path("cloud_name") cloudName: String,

        // (nuevo) El archivo de imagen como MultipartBody.Part.
        // (nuevo) Se construye en el Repository (subpaso 10.7).
        @Part image: MultipartBody.Part,

        // (nuevo) El nombre del upload preset (modo unsigned).
        // (nuevo) Cloudinary usa esto para validar que la subida está permitida.
        @Part("upload_preset") uploadPreset: RequestBody
    ): CloudinaryUploadResponse
}