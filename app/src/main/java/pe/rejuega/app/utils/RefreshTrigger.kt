// (nuevo) Bus de eventos mínimo: avisa a las pantallas que deben recargar.
package pe.rejuega.app.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object RefreshTrigger {
    private val _eventos = MutableStateFlow(0L)
    val eventos: StateFlow<Long> = _eventos

    // (modificado) Con log para confirmar que el formulario avisa.
    fun notificar() {
        println(">>> REFRESH-A: notificar() llamado desde el formulario")
        _eventos.value = System.currentTimeMillis()
    }
}
