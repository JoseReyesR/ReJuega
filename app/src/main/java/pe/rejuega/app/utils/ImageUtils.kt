// (nuevo) Archivo: ImageUtils.kt
// (nuevo) Descripción: Utilidad para comprimir imágenes y crear URIs temporales para la cámara.
// (nuevo) Implementa la Regla de Negocio RB-09: Las imágenes se comprimen antes de subirse.
package pe.rejuega.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

// (nuevo) Objeto singleton con funciones de procesamiento de imágenes.
// (nuevo) Se accede sin crear instancia: ImageUtils.compressImage(...)
object ImageUtils {

    // =====================================================================
    // (nuevo) FUNCIÓN 1: COMPRIMIR IMAGEN
    // =====================================================================
    // (nuevo) Toma un Uri de la galería o de la cámara, lo decodifica,
    // (nuevo) lo escala a un ancho máximo y lo comprime en formato JPEG.
    // (nuevo) Retorna un ByteArray listo para ser subido a Cloudinary.
    // (nuevo) Parámetros:
    // (nuevo)   context  - Contexto de la app (para abrir el Uri).
    // (nuevo)   uri      - Uri de la imagen (galería o cámara).
    // (nuevo)   maxWidth - Ancho máximo en píxeles. Por defecto 800.
    // (nuevo)   quality  - Calidad JPEG de 0 a 100. Por defecto 70.
    // =====================================================================
    fun compressImage(
        context: Context,
        uri: Uri,
        maxWidth: Int = 800,
        quality: Int = 70
    ): ByteArray? {
        return try {
            // (nuevo) Paso 1: Abrir el flujo de entrada del Uri.
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            // (nuevo) Paso 2: Decodificar el flujo de entrada en un Bitmap.
            // (nuevo) BitmapFactory lee la imagen y la convierte en memoria.
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // (nuevo) Si la decodificación falló (imagen corrupta), retornamos null.
            if (originalBitmap == null) return null

            // (nuevo) Paso 3: Calcular nuevas dimensiones manteniendo la proporción.
            // (nuevo) Solo escalamos si la imagen es más ancha que maxWidth.
            val originalWidth = originalBitmap.width
            val originalHeight = originalBitmap.height

            val newWidth: Int
            val newHeight: Int

            if (originalWidth > maxWidth) {
                // (nuevo) Calculamos la proporción alto/ancho.
                val aspectRatio = originalHeight.toFloat() / originalWidth.toFloat()
                newWidth = maxWidth
                newHeight = (maxWidth * aspectRatio).toInt()
            } else {
                // (nuevo) Si la imagen ya es pequeña, no la escalamos.
                newWidth = originalWidth
                newHeight = originalHeight
            }

            // (nuevo) Paso 4: Escalar el Bitmap a las nuevas dimensiones.
            // (nuevo) createScaledBitmap crea una copia escalada.
            val scaledBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                newWidth,
                newHeight,
                true // (nuevo) true = usar filtrado bilineal para mejor calidad
            )

            // (nuevo) Paso 5: Comprimir el Bitmap escalado en un ByteArray.
            // (nuevo) Usamos formato JPEG con la calidad especificada.
            val byteArrayOutputStream = ByteArrayOutputStream()
            scaledBitmap.compress(
                Bitmap.CompressFormat.JPEG,
                quality,
                byteArrayOutputStream
            )

            // (nuevo) Paso 6: Liberar memoria de los Bitmaps para evitar fugas.
            // (nuevo) recycle() libera la memoria nativa del Bitmap.
            if (scaledBitmap !== originalBitmap) {
                originalBitmap.recycle()
            }
            scaledBitmap.recycle()

            // (nuevo) Paso 7: Retornar el array de bytes comprimido.
            byteArrayOutputStream.toByteArray()

        } catch (e: Exception) {
            // (nuevo) Si ocurre cualquier error (Uri inválido, imagen corrupta),
            // (nuevo) imprimimos el error y retornamos null.
            e.printStackTrace()
            null
        }
    }

    // =====================================================================
    // (nuevo) FUNCIÓN 2: CREAR URI TEMPORAL PARA LA CÁMARA
    // =====================================================================
    // (nuevo) Crea un archivo temporal vacío y retorna su Uri segura.
    // (nuevo) La cámara del sistema escribirá la foto en esta Uri.
    // (nuevo) Usamos FileProvider porque Android 7+ no permite pasar rutas
    // (nuevo) de archivo directas entre apps por seguridad.
    // =====================================================================
    fun createTempImageUri(context: Context): Uri {
        // (nuevo) Paso 1: Crear un archivo temporal en el directorio de caché.
        // (nuevo) createTempFile genera un nombre único automáticamente.
        val imageFile = File.createTempFile(
            "rejuega_foto_",   // (nuevo) Prefijo del nombre del archivo
            ".jpg",            // (nuevo) Extensión
            context.cacheDir   // (nuevo) Directorio: caché interna de la app
        )

        // (nuevo) Paso 2: Convertir el File a una Uri segura usando FileProvider.
        // (nuevo) La autoridad debe coincidir con la del AndroidManifest.xml.
        // (nuevo) AndroidManifest tiene: ${applicationId}.fileprovider
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }
}