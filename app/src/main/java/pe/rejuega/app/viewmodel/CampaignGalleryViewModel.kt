// (nuevo) ViewModel para gestionar la galería de una campaña.
package pe.rejuega.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.remote.CampaignEvidence
import pe.rejuega.app.data.repository.CampaignGalleryRepository

class CampaignGalleryViewModel(
    private val repository: CampaignGalleryRepository,
    private val campaignId: String,
    private val campaignTitulo: String,
    private val uidAdmin: String? = null
) : ViewModel() {

    private val _evidencias = MutableStateFlow<List<CampaignEvidence>>(emptyList())
    val evidencias: StateFlow<List<CampaignEvidence>> = _evidencias.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    init { observar() }

    private fun observar() {
        viewModelScope.launch {
            repository.observarPorCampaign(campaignId).collect {
                _evidencias.value = it
            }
        }
    }

    // (nuevo) Sube varias fotos con una descripción compartida.
    fun subirEvidencias(context: Context, uris: List<Uri>, descripcion: String) {
        if (uidAdmin == null) {
            _mensaje.value = "Solo el administrador puede subir evidencias."
            return
        }
        viewModelScope.launch {
            _isUploading.value = true
            var exitosas = 0
            for (uri in uris) {
                try {
                    repository.subirEvidencia(
                        context, uidAdmin, campaignId, campaignTitulo, uri, descripcion
                    )
                    exitosas++
                } catch (e: Exception) {
                    _mensaje.value = "Error subiendo imagen: ${e.message}"
                }
            }
            _isUploading.value = false
            if (exitosas > 0) {
                _mensaje.value = "$exitosas evidencias subidas correctamente."
            }
        }
    }

    fun eliminarEvidencia(id: String) {
        if (uidAdmin == null) return
        viewModelScope.launch {
            try {
                repository.eliminar(id)
            } catch (e: Exception) {
                _mensaje.value = "Error al eliminar: ${e.message}"
            }
        }
    }

    fun limpiarMensaje() { _mensaje.value = null }
}

class CampaignGalleryViewModelFactory(
    private val repository: CampaignGalleryRepository,
    private val campaignId: String,
    private val campaignTitulo: String,
    private val uidAdmin: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CampaignGalleryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CampaignGalleryViewModel(repository, campaignId, campaignTitulo, uidAdmin) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}
