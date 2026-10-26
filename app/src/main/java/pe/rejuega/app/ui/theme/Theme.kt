// (nuevo) Archivo: Theme.kt
// (nuevo) Descripción: Configuración del tema Material 3 para ReJuega
// (nuevo) Paquete: pe.rejuega.app.ui.theme

package pe.rejuega.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// (nuevo) ESQUEMA DE COLORES MODO CLARO
private val LightColorScheme = lightColorScheme(
    primary = VerdeEsperanza,           // (nuevo) Color principal de la app
    onPrimary = BlancoPuro,             // (nuevo) Texto sobre el color primario
    primaryContainer = VerdeClaro,      // (nuevo) Fondo de contenedores primarios
    onPrimaryContainer = VerdeOscuro,   // (nuevo) Texto sobre contenedor primario
    secondary = NaranjaJuguete,         // (nuevo) Color secundario para acentos
    onSecondary = BlancoPuro,           // (nuevo) Texto sobre color secundario
    secondaryContainer = NaranjaClaro,  // (nuevo) Fondo de contenedores secundarios
    onSecondaryContainer = NaranjaOscuro, // (nuevo) Texto sobre contenedor secundario
    background = BlancoHumo,            // (nuevo) Fondo general de la pantalla
    onBackground = GrisOscuro,          // (nuevo) Texto principal sobre el fondo
    surface = BlancoPuro,               // (nuevo) Superficie de tarjetas y diálogos
    onSurface = GrisOscuro              // (nuevo) Texto sobre superficies
)

// (nuevo) ESQUEMA DE COLORES MODO OSCURO
private val DarkColorScheme = darkColorScheme(
    primary = VerdeClaro,
    onPrimary = VerdeOscuro,
    primaryContainer = VerdeOscuro,
    onPrimaryContainer = VerdeClaro,
    secondary = NaranjaClaro,
    onSecondary = NaranjaOscuro,
    secondaryContainer = NaranjaOscuro,
    onSecondaryContainer = NaranjaClaro,
    background = NegroPuro,
    onBackground = BlancoPuro,
    surface = Color(0xFF1E1E1E),
    onSurface = BlancoPuro
)

// (nuevo) FUNCIÓN PRINCIPAL DEL TEMA QUE ENVUELVE TODA LA APP
@Composable
fun ReJuegaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // (nuevo) Detecta si el sistema está en modo oscuro
    dynamicColor: Boolean = false,              // (nuevo) Desactivado para forzar nuestra identidad de marca
    content: @Composable () -> Unit             // (nuevo) Contenido de la interfaz que se renderizará
) {
    // (nuevo) Seleccionar el esquema de colores según el modo
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // (nuevo) Ajustar el color de la barra de estado del sistema para que combine
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // (nuevo) Aplicar el tema Material 3 a todo el contenido
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // (nuevo) Tipografía por defecto de Material 3
        content = content
    )
}