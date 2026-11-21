// (modificado) Archivo: JugueteViewModel.kt
// (modificado) Cambio: Ahora extiende de AndroidViewModel para tener acceso al Context.
// (modificado) Cambio: Usa DonationRepository para subir a Cloudinary + Firestore + Room.
// (modificado) Motivo: Implementar el registro real de juguetes con foto en la nube.
package pe.rejuega.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.rejuega.app.data.remote.DonationRepository
import pe.rejuega.app.data.remote.MaterialJugueteDto
import pe.rejuega.app.data.repository.MaterialEcoRepository
import pe.rejuega.app.utils.DatosSemilla

// (modificado) Estado de la interfaz del formulario de registro.
// (modificado) Se mantiene igual que antes. No cambia la estructura.
data class RegisterToyUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val estado: String = "Nuevo",
    val fotoUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

// (modificado) Ahora hereda de AndroidViewModel en lugar de ViewModel.
// (modificado) AndroidViewModel proporciona acceso al Context de la aplicación,
// (modificado) necesario para comprimir imágenes y crear URIs temporales.
class JugueteViewModel(
    application: Application,
    private val donationRepository: DonationRepository,
    private val materialEcoRepository: MaterialEcoRepository,
    private val campaignId: Long,
    private val campaignTitulo: String,
    private val campaignFirestoreId: String     // (nuevo)
) : AndroidViewModel(application) {

    // (modificado) Estado interno del formulario.
    private val _uiState = MutableStateFlow(RegisterToyUiState())

    // (modificado) Estado público de solo lectura para la pantalla.
    val uiState: StateFlow<RegisterToyUiState> = _uiState.asStateFlow()

    // (nuevo) Materiales disponibles para el dropdown.
    private val _materiales = MutableStateFlow<List<MaterialJugueteDto>>(emptyList())
    val materiales: StateFlow<List<MaterialJugueteDto>> = _materiales.asStateFlow()

    // (nuevo) Material elegido por el usuario.
    private val _materialSeleccionado = MutableStateFlow<MaterialJugueteDto?>(null)
    val materialSeleccionado: StateFlow<MaterialJugueteDto?> = _materialSeleccionado.asStateFlow()

    init { cargarMateriales() }

    // (nuevo) Carga de la API con respaldo local sin internet.
    private fun cargarMateriales() {
        viewModelScope.launch {
            _materiales.value = try {
                materialEcoRepository.obtenerMaterialesUnaVez()
                    .ifEmpty { DatosSemilla.listaMaterialesDto }
            } catch (e: Exception) {
                DatosSemilla.listaMaterialesDto
            }
        }
    }

    // (nuevo) Selección del material.
    fun onMaterialSeleccionado(material: MaterialJugueteDto) {
        _materialSeleccionado.value = material
        _uiState.update { it.copy(error = null) }
    }

    // (modificado) Actualiza el nombre cuando el usuario escribe.
    fun onNombreChange(nuevoNombre: String) {
        _uiState.update { it.copy(nombre = nuevoNombre, error = null) }
    }

    // (modificado) Actualiza la descripción cuando el usuario escribe.
    fun onDescripcionChange(nuevaDescripcion: String) {
        _uiState.update { it.copy(descripcion = nuevaDescripcion, error = null) }
    }

    // (modificado) Actualiza el estado del juguete (Nuevo / Buen Estado).
    fun onEstadoChange(nuevoEstado: String) {
        _uiState.update { it.copy(estado = nuevoEstado, error = null) }
    }

    // (modificado) Guarda la URI de la foto seleccionada o tomada.
    // (modificado) Se llama desde la pantalla cuando el usuario elige una imagen.
    fun onFotoSeleccionada(uri: Uri?) {
        _uiState.update { it.copy(fotoUri = uri, error = null) }
    }

    // =====================================================================
    // (modificado) MÉTODO PRINCIPAL: REGISTRAR JUGUETE
    // =====================================================================
    // (modificado) Valida los campos (RB-01) y llama al DonationRepository.
    // (modificado) El Repository hace la compresión, subida y guardado.
    // (modificado) Parámetro:
    // (modificado)   uidUsuario - UID del usuario autenticado en Firebase.
    // =====================================================================
    fun registrarJuguete(uidUsuario: String) {
        val estadoActual = _uiState.value

        // (modificado) VALIDACIÓN 1: Nombre obligatorio.
        if (estadoActual.nombre.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa el nombre del juguete.") }
            return
        }

        // (modificado) VALIDACIÓN 2: Descripción obligatoria.
        if (estadoActual.descripcion.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa una descripción breve del juguete.") }
            return
        }

        // (modificado) VALIDACIÓN 3: Estado permitido (RB-01).
        // (modificado) Solo se permiten "Nuevo" o "Buen Estado".
        if (estadoActual.estado != "Nuevo" && estadoActual.estado != "Buen Estado") {
            _uiState.update {
                it.copy(error = "El juguete solo puede estar en estado Nuevo o Buen Estado.")
            }
            return
        }

        // (modificado) VALIDACIÓN 4: Fotografía obligatoria (RB-01).
        if (estadoActual.fotoUri == null) {
            _uiState.update {
                it.copy(error = "Debes seleccionar o tomar al menos una fotografía del juguete.")
            }
            return
        }

        // (nuevo) VALIDACIÓN 5: Material obligatorio.
        if (_materialSeleccionado.value == null) {
            _uiState.update {
                it.copy(error = "Selecciona el material del juguete.")
            }
            return
        }

        // (modificado) Si pasa todas las validaciones, iniciamos el registro.
        viewModelScope.launch {
            try {
                // (modificado) Indicamos que estamos cargando.
                _uiState.update { it.copy(isLoading = true, error = null) }

                val material = _materialSeleccionado.value!!

                // (modificado) Llamamos al Repository que hace todo el flujo:
                // (modificado) comprimir → subir a Cloudinary → Firestore → Room.
                donationRepository.registrarPropuesta(
                    context = getApplication<Application>().applicationContext,
                    uid = uidUsuario,
                    campaignId = campaignId,
                    campaignTitulo = campaignTitulo,   // (nuevo)
                    nombre = estadoActual.nombre,
                    descripcion = estadoActual.descripcion,
                    estado = estadoActual.estado,
                    fotoUri = estadoActual.fotoUri!!,
                    materialTipo = material.tipoMaterial,
                    co2AhorradoKg = material.co2AhorradoKg,
                    campaignFirestoreId = campaignFirestoreId     // (nuevo)
                )

                // (modificado) Si todo sale bien, marcamos éxito.
                // (modificado) La pantalla observará isSuccess para navegar de regreso.
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }

            } catch (e: Exception) {
                // (modificado) Si algo falla (sin internet, error de Cloudinary, etc.),
                // (modificado) mostramos un mensaje de error amigable.
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al registrar la donación: ${e.message}"
                    )
                }
            }
        }
    }
}

// (modificado) Factory para crear el ViewModel con sus dependencias.
// (modificado) Recibe Application, DonationRepository y campaignId.
class JugueteViewModelFactory(
    private val application: Application,
    private val donationRepository: DonationRepository,
    private val materialEcoRepository: MaterialEcoRepository,
    private val campaignId: Long,
    private val campaignTitulo: String,
    private val campaignFirestoreId: String     // (nuevo)
) : ViewModelProvider.Factory {

    // (modificado) Android llama a este método cuando necesita crear el ViewModel.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JugueteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JugueteViewModel(application, donationRepository, materialEcoRepository, campaignId, campaignTitulo, campaignFirestoreId) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}