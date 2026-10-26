// (nuevo) Archivo: JugueteViewModel.kt
// (nuevo) Descripción: ViewModel para registrar una propuesta de juguete.
// (nuevo) Valida la regla de negocio RB-01:
// (nuevo) - El juguete debe estar en estado "Nuevo" o "Buen Estado".
// (nuevo) - Debe incluir al menos una fotografía.
package pe.rejuega.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.rejuega.app.data.local.Juguete
import pe.rejuega.app.data.repository.JugueteRepository

// (nuevo) Estado de la interfaz del formulario de registro de juguete.
data class RegisterToyUiState(
    val nombre: String = "",
    val descripcion: String = "",
    val estado: String = "Nuevo",
    val fotoUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

// (nuevo) ViewModel para el registro de juguetes.
class JugueteViewModel(
    private val jugueteRepository: JugueteRepository,
    private val campaignId: Long
) : ViewModel() {

    // (nuevo) Estado inicial del formulario.
    private val _uiState = MutableStateFlow(RegisterToyUiState())

    // (nuevo) Estado público observado por la pantalla.
    val uiState: StateFlow<RegisterToyUiState> = _uiState.asStateFlow()

    // (nuevo) Actualiza el nombre cuando el usuario escribe.
    fun onNombreChange(nuevoNombre: String) {
        _uiState.update {
            it.copy(nombre = nuevoNombre, error = null)
        }
    }

    // (nuevo) Actualiza la descripción cuando el usuario escribe.
    fun onDescripcionChange(nuevaDescripcion: String) {
        _uiState.update {
            it.copy(descripcion = nuevaDescripcion, error = null)
        }
    }

    // (nuevo) Actualiza el estado del juguete.
    fun onEstadoChange(nuevoEstado: String) {
        _uiState.update {
            it.copy(estado = nuevoEstado, error = null)
        }
    }

    // (nuevo) Guarda la URI de la imagen seleccionada por el usuario.
    fun onFotoSeleccionada(uri: Uri?) {
        _uiState.update {
            it.copy(fotoUri = uri, error = null)
        }
    }

    // (nuevo) Valida y registra el juguete en la base de datos local.
    fun registrarJuguete() {
        val estadoActual = _uiState.value

        // (nuevo) Validación: nombre obligatorio.
        if (estadoActual.nombre.isBlank()) {
            _uiState.update {
                it.copy(error = "Ingresa el nombre del juguete.")
            }
            return
        }

        // (nuevo) Validación: descripción obligatoria.
        if (estadoActual.descripcion.isBlank()) {
            _uiState.update {
                it.copy(error = "Ingresa una descripción breve del juguete.")
            }
            return
        }

        // (nuevo) Validación RB-01: estado permitido.
        if (estadoActual.estado != "Nuevo" && estadoActual.estado != "Buen Estado") {
            _uiState.update {
                it.copy(error = "El juguete solo puede estar en estado Nuevo o Buen Estado.")
            }
            return
        }

        // (nuevo) Validación RB-01: fotografía obligatoria.
        if (estadoActual.fotoUri == null) {
            _uiState.update {
                it.copy(error = "Debes seleccionar al menos una fotografía del juguete.")
            }
            return
        }

        // (nuevo) Si pasa las validaciones, iniciamos el guardado.
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(isLoading = true, error = null)
                }

                // (nuevo) Creamos el objeto Juguete que se guardará en Room.
                val juguete = Juguete(
                    campaignId = campaignId,
                    nombre = estadoActual.nombre.trim(),
                    descripcion = estadoActual.descripcion.trim(),
                    estado = estadoActual.estado,
                    fotoUrl = estadoActual.fotoUri.toString(),
                    estadoTrazabilidad = "Pendiente"
                )

                // (nuevo) Insertamos el juguete mediante el repositorio.
                jugueteRepository.insertarJuguete(juguete)

                // (nuevo) Marcamos éxito para que la pantalla pueda regresar.
                _uiState.update {
                    it.copy(isLoading = false, isSuccess = true)
                }
            } catch (e: Exception) {
                // (nuevo) Si ocurre un error, se muestra un mensaje amigable.
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "No se pudo registrar el juguete: ${e.message}"
                    )
                }
            }
        }
    }
}

// (nuevo) Factory para crear el ViewModel con dependencias y campaignId.
class JugueteViewModelFactory(
    private val jugueteRepository: JugueteRepository,
    private val campaignId: Long
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JugueteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JugueteViewModel(jugueteRepository, campaignId) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}