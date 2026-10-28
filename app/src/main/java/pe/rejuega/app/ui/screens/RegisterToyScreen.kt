// (modificado) Archivo: RegisterToyScreen.kt
// (modificado) Cambio: Se agregan opciones de Cámara y Galería para adjuntar fotos.
// (modificado) Implementa HU-05 criterio (4): si se deniega el permiso de cámara,
// (modificado) la app muestra un mensaje y no se cierra.
// (modificado) Implementa RN-06: solicitud de permisos en tiempo de ejecución.
package pe.rejuega.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import pe.rejuega.app.ui.viewmodel.JugueteViewModel
import pe.rejuega.app.utils.ImageUtils
// (nuevo) Imports necesarios para el AlertDialog de confirmación.
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
// (modificado) OptIn necesario para TopAppBar de Material 3.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterToyScreen(
    viewModel: JugueteViewModel,
    uidUsuario: String,
    onBackClick: () -> Unit,
    onSaved: () -> Unit
) {
    // (modificado) Observamos el estado del formulario desde el ViewModel.
    val uiState by viewModel.uiState.collectAsState()

    // (nuevo) Contexto necesario para FileProvider y permisos de cámara.
    val context = LocalContext.current

    // (nuevo) Variable para almacenar la URI temporal donde la cámara guardará la foto.
    // (nuevo) Se crea justo antes de lanzar la cámara y se usa cuando la cámara retorna.
    var tempCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // (nuevo) Variable para mostrar mensaje cuando se deniega el permiso de cámara.
    // (nuevo) Implementa HU-05 criterio (4): mensaje amigable sin cerrar la app.
    var mensajePermisoDenegado by rememberSaveable { mutableStateOf<String?>(null) }

    // =====================================================================
    // (modificado) LAUNCHER 1: SELECCIONAR IMAGEN DE LA GALERÍA
    // =====================================================================
    // (modificado) GetContent abre el selector de imágenes del sistema.
    // (modificado) No requiere permisos de almacenamiento en Android 13+.
    // =====================================================================
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // (modificado) Pasamos la URI seleccionada al ViewModel.
        viewModel.onFotoSeleccionada(uri)
    }

    // =====================================================================
    // (nuevo) LAUNCHER 2: TOMAR FOTO CON LA CÁMARA
    // =====================================================================
    // (nuevo) TakePicture abre la cámara del sistema.
    // (nuevo) Recibe una URI donde la cámara escribirá la foto.
    // (nuevo) Retorna true si el usuario tomó la foto y la aceptó.
    // =====================================================================
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            // (nuevo) Si la foto se tomó correctamente, pasamos la URI al ViewModel.
            viewModel.onFotoSeleccionada(tempCameraUri)
        }
        // (nuevo) Si success es false, el usuario canceló la cámara. No hacemos nada.
    }

    // =====================================================================
    // (nuevo) LAUNCHER 3: SOLICITAR PERMISO DE CÁMARA
    // =====================================================================
    // (nuevo) RequestPermission muestra el diálogo nativo de permisos de Android.
    // (nuevo) Solo se usa si el permiso no ha sido concedido aún.
    // =====================================================================
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // (nuevo) Permiso concedido: creamos URI temporal y lanzamos la cámara.
            val uri = ImageUtils.createTempImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            // (nuevo) Permiso denegado: mostramos mensaje amigable.
            // (nuevo) HU-05 criterio (4): la app NO se cierra.
            mensajePermisoDenegado = "Se necesita permiso de cámara para tomar fotos. " +
                    "Puedes seleccionar una imagen de la galería."
        }
    }



    // (modificado) Estructura base de la pantalla.
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Registrar juguete",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // (modificado) Texto introductorio.
            Text(
                text = "Completa los datos del juguete que deseas donar.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // (modificado) Campo nombre del juguete.
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = viewModel::onNombreChange,
                label = { Text("Nombre del juguete") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // (modificado) Campo descripción del juguete.
            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = viewModel::onDescripcionChange,
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // (modificado) Selección de estado del juguete (RB-01).
            Text(
                text = "Estado del juguete",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = uiState.estado == "Nuevo",
                    onClick = { viewModel.onEstadoChange("Nuevo") }
                )
                Text(text = "Nuevo")

                Spacer(modifier = Modifier.size(16.dp))

                RadioButton(
                    selected = uiState.estado == "Buen Estado",
                    onClick = { viewModel.onEstadoChange("Buen Estado") }
                )
                Text(text = "Buen Estado")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =====================================================================
            // (nuevo) SECCIÓN DE FOTOGRAFÍA: CÁMARA + GALERÍA
            // =====================================================================
            Text(
                text = "Fotografía del juguete",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // (nuevo) Fila con dos botones: Cámara y Galería.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // (nuevo) Botón para tomar foto con cámara.
                OutlinedButton(
                    onClick = {
                        // (nuevo) Limpiamos mensaje de error anterior.
                        mensajePermisoDenegado = null

                        // (nuevo) Verificamos si el permiso de cámara ya está concedido.
                        val permissionGranted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (permissionGranted) {
                            // (nuevo) Permiso ya concedido: creamos URI y lanzamos cámara.
                            val uri = ImageUtils.createTempImageUri(context)
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            // (nuevo) Permiso no concedido: lo solicitamos (RN-06).
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Tomar foto con cámara",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cámara")
                }

                // (modificado) Botón para seleccionar imagen de la galería.
                OutlinedButton(
                    onClick = {
                        // (nuevo) Limpiamos mensaje de error anterior.
                        mensajePermisoDenegado = null
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Seleccionar de galería",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Galería")
                }
            }

            // (nuevo) Mostrar mensaje si el permiso de cámara fue denegado.
            if (mensajePermisoDenegado != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mensajePermisoDenegado ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // (modificado) Vista previa de la imagen seleccionada o tomada.
            if (uiState.fotoUri != null) {
                AsyncImage(
                    model = uiState.fotoUri,
                    contentDescription = "Foto del juguete",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "No has seleccionado ninguna fotografía. Usa la cámara o la galería.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // (modificado) Advertencia de privacidad RB-06.
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Importante: La fotografía no debe mostrar rostros de menores " +
                            "de edad. Esta advertencia cumple con la regla de negocio RB-06 " +
                            "y la política de privacidad del proyecto.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // (modificado) Mostrar error de validación si existe.
            if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // (modificado) Botón para registrar el juguete.
            Button(
                onClick = { viewModel.registrarJuguete(uidUsuario) },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    // (modificado) Mientras se sube la foto, mostramos un spinner.
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Registrar propuesta de donación")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // (nuevo) AlertDialog de confirmación tras registrar exitosamente.
    // Motivo: el usuario necesita ver un mensaje de éxito antes de salir.

    if (uiState.isSuccess) {
        AlertDialog(
            // (nuevo) No permitimos cerrar el diálogo tocando fuera de él.
            // (nuevo) El usuario DEBE presionar "Entendido" para continuar.
            onDismissRequest = { },

            // (nuevo) Ícono de verificación para reforzar el mensaje de éxito.
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Éxito",
                    tint = MaterialTheme.colorScheme.primary
                )
            },

            // (nuevo) Título del diálogo.
            title = {
                Text(
                    text = "¡Donación registrada!",
                    fontWeight = FontWeight.Bold
                )
            },

            // (nuevo) Mensaje explicativo para el usuario.
            // (nuevo) Se menciona el siguiente paso en el flujo de trazabilidad.
            text = {
                Text(
                    text = "Tu juguete fue enviado correctamente a la ONG para su revisión.\n\n" +
                            "Puedes seguir su estado desde la pantalla de trazabilidad."
                )
            },

            // (nuevo) Botón de confirmación.
            // (nuevo) Al presionarlo, se ejecuta onSaved() que navega de vuelta.
            confirmButton = {
                Button(
                    onClick = {
                        // (nuevo) Aquí SÍ se navega de vuelta, pero solo después
                        // (nuevo) de que el usuario haya visto y aceptado el mensaje.
                        onSaved()
                    }
                ) {
                    Text("Entendido")
                }
            }
        )
    }
}