// (nuevo) Archivo: UiState.kt
// (nuevo) Descripción: Clase sellada (sealed class) que representa los estados de una pantalla.
// (nuevo) Alineado con el Sprint 4 del backlog: Loading, Success, Error, Empty.
// (nuevo) La usaremos en todos los ViewModels para manejar el estado de la UI.
package pe.rejuega.app.utils

// (nuevo) 'sealed class' significa que solo estas 4 subclases pueden existir.
// (nuevo) El parámetro genérico <T> permite que el estado contenga cualquier tipo de dato.
sealed class UiState<out T> {

    // (nuevo) Estado: La pantalla está cargando datos (mostrar un spinner)
    data object Loading : UiState<Nothing>()

    // (nuevo) Estado: Los datos se cargaron correctamente (mostrar el contenido)
    data class Success<T>(val data: T) : UiState<T>()

    // (nuevo) Estado: Ocurrió un error (mostrar mensaje y botón de reintento)
    data class Error(val message: String) : UiState<Nothing>()

    // (nuevo) Estado: No hay datos para mostrar (mostrar pantalla vacía)
    data object Empty : UiState<Nothing>()
}