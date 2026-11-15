// (nuevo) Repository de galería de evidencias (HU-09).
package pe.rejuega.app.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import pe.rejuega.app.data.remote.CampaignEvidence
import pe.rejuega.app.data.remote.RetrofitClient
import pe.rejuega.app.utils.ImageUtils
import java.io.File
import java.io.IOException

class CampaignGalleryRepository(
    private val firestore: FirebaseFirestore
) {
    private companion object {
        const val CLOUD_NAME = "eg8sugwt"
        const val UPLOAD_PRESET = "rejuega_preset"
    }

    private val collection = firestore.collection("campaignGallery")

    // (nuevo) Observa las evidencias de una campaña (ordenadas por fecha DESC en memoria).
    fun observarPorCampaign(campaignId: String): Flow<List<CampaignEvidence>> = callbackFlow {
        val listener = collection
            .whereEqualTo("campaignId", campaignId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error observando galería: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CampaignEvidence::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.fechaSubida } ?: emptyList()
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }

    // (nuevo) Sube una foto a Cloudinary y guarda el registro en Firestore.
    suspend fun subirEvidencia(
        context: Context,
        uidAdmin: String,
        campaignId: String,
        campaignTitulo: String,
        fotoUri: Uri,
        descripcion: String
    ) {
        // (nuevo) Comprimir y convertir a archivo temporal.
        val bytes = ImageUtils.compressImage(context, fotoUri)
            ?: throw IOException("No se pudo procesar la imagen.")
        val tempFile = File.createTempFile("rejuega_evidence_", ".jpg", context.cacheDir)
        tempFile.writeBytes(bytes)

        // (nuevo) Subida multipart a Cloudinary.
        val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
        val presetBody = UPLOAD_PRESET.toRequestBody("text/plain".toMediaTypeOrNull())
        val response = RetrofitClient.getCloudinaryService()
            .uploadImage(CLOUD_NAME, imagePart, presetBody)
        tempFile.delete()

        // (nuevo) Guardar el registro en Firestore.
        val evidencia = CampaignEvidence(
            campaignId = campaignId,
            campaignTitulo = campaignTitulo,
            fotoUrl = response.secure_url,
            descripcion = descripcion.trim(),
            fechaSubida = System.currentTimeMillis(),
            subidoPor = uidAdmin
        )
        collection.add(evidencia).await()
    }

    // (nuevo) Baja lógica: marca la evidencia como inactiva (opcional, no implementado en UI).
    suspend fun eliminar(id: String) {
        collection.document(id).delete().await()
    }
}
