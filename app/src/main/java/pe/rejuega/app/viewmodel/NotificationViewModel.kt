// (nuevo) ViewModel del centro de notificaciones del donante.
package pe.rejuega.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.rejuega.app.data.remote.Notificacion
import pe.rejuega.app.data.repository.NotificationFirestoreRepository

class NotificationViewModel(
    private val repository: NotificationFirestoreRepository,
    private val uid: String
) : ViewModel() {

    private val _notificaciones = MutableStateFlow<List<Notificacion>>(emptyList())
    val notificaciones: StateFlow<List<Notificacion>> = _notificaciones.asStateFlow()

    init { observar() }

    private fun observar() {
        viewModelScope.launch {
            try {
                repository.observarPorUsuario(uid).collect { _notificaciones.value = it }
            } catch (e: Exception) {
                println("Error en ViewModel de notificaciones: ${e.message}")
            }
        }
    }

    fun marcarLeida(id: String) {
        viewModelScope.launch { repository.marcarLeida(id) }
    }

    fun marcarTodasLeidas() {
        viewModelScope.launch { repository.marcarTodasLeidas(uid) }
    }
}

class NotificationViewModelFactory(
    private val repository: NotificationFirestoreRepository,
    private val uid: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(repository, uid) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}