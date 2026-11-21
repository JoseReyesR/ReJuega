// (nuevo) Repository de galería de evidencias en Node (primario) y Firestore (respaldo).
package pe.rejuega.app.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import pe.rejuega.app.data.remote.CampaignEvidence
import pe.rejuega.app.data.remote.CampaignEvidenceNodeDto
import pe.rejuega.app.data.remote.RetrofitClient
import pe.rejuega.app.utils.ImageUtils
import java.io.File
import java.io.IOException
import java.util.UUID

class CampaignGalleryRepository(
    private val firestore: FirebaseFirestore
) {
    private companion object {
        const val CLOUD_NAME = "eg8sugwt"
        const val UPLOAD_PRESET = "rejuega_preset"
    }

    private val collection = firestore.collection("campaignGallery")
    private val node = RetrofitClient.getNodeApiService()

    // (modificado) Mapeos entre Node DTO y modelo de la app.
    private fun CampaignEvidenceNodeDto.aFirestore(): CampaignEvidence = CampaignEvidence(
        id = id, campaignId = campaignId, campaignTitulo = "",
        fotoUrl = fotoUrl, descripcion = descripcion ?: "",
        fechaSubida = fechaSubida?.toLongOrNull() ?: 0, subidoPor = subidoPor ?: ""
    )

    private fun CampaignEvidence.aNode(): CampaignEvidenceNodeDto = CampaignEvidenceNodeDto(
        id = id, campaignId = campaignId, fotoUrl = fotoUrl,
        descripcion = descripcion, subidoPor = subidoPor
    )

    // (modificado) Observar evidencias: Node primario, Firestore respaldo.
    fun observarPorCampaign(campaignId: String): Flow<List<CampaignEvidence>> = flow {
        try {
            val resp = node.obtenerEvidencias(campaignId)
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            emit(resp.body()?.data?.map { it.aFirestore() } ?: emptyList())
        } catch (e: Exception) {
            // (respaldo) Firestore.
            try {
                val snapshot = collection
                    .whereEqualTo("campaignId", campaignId)
                    .orderBy("fechaSubida", Query.Direction.DESCENDING)
                    .get().await()
                emit(snapshot.documents.mapNotNull {
                    it.toObject(CampaignEvidence::class.java)?.copy(id = it.id)
                })
            } catch (e2: Exception) { emit(emptyList()) }
        }
    }

    // (modificado) Subir evidencia: Node primario + copia en Firestore.
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

        val id = UUID.randomUUID().toString()
        val evidencia = CampaignEvidence(
            id = id,
            campaignId = campaignId,
            campaignTitulo = campaignTitulo,
            fotoUrl = response.secure_url,
            descripcion = descripcion.trim(),
            fechaSubida = System.currentTimeMillis(),
            subidoPor = uidAdmin
        )

        try {
            // (nuevo) Escribe en Node.
            val resp = node.crearEvidencia(evidencia.aNode())
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            println(">>> GALLERY CREATE OK: $id")
            // (respaldo) Firestore.
            try { collection.document(id).set(evidencia).await() } catch (_: Exception) {}
        } catch (e: Exception) {
            println(">>> GALLERY CREATE FAIL: ${e.message} → uso Firestore")
            collection.document(id).set(evidencia).await()
        }
    }

    // (modificado) Eliminar: Node primario + borrado en Firestore.
    suspend fun eliminar(id: String) {
        try {
            val resp = node.eliminarEvidencia(id)
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code()}")
            println(">>> GALLERY DELETE OK: $id")
            try { collection.document(id).delete().await() } catch (_: Exception) {}
        } catch (e: Exception) {
            println(">>> GALLERY DELETE FAIL: ${e.message} → uso Firestore")
            collection.document(id).delete().await()
        }
    }
}
