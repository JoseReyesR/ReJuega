// (nuevo) Mapa osmdroid con marcadores de puntos de recojo y recomendación (HU-10).
package pe.rejuega.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import pe.rejuega.app.viewmodel.MapaViewModel
import java.util.Locale

// (nuevo) Centro por defecto: Trujillo, Perú.
private val TRUJILLO = GeoPoint(-8.1116, -79.0288)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaPuntosRecojoScreen(
    viewModel: MapaViewModel,
    onBackClick: () -> Unit
) {
    val puntos by viewModel.puntos.collectAsState()
    val recomendado by viewModel.recomendado.collectAsState()
    val distancia by viewModel.distanciaMetros.collectAsState()
    val context = LocalContext.current

    var permisoDenegado by remember { mutableStateOf(false) }
    var centroUsuario by remember { mutableStateOf<GeoPoint?>(null) }
    var yaCentrado by remember { mutableStateOf(false) }

    // (nuevo) Launcher del permiso de ubicación (RN-07).
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            // (nuevo) Una sola lectura de la última ubicación (sin rastreo continuo).
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                client.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        centroUsuario = GeoPoint(it.latitude, it.longitude)
                        viewModel.calcularRecomendado(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {
                permisoDenegado = true
            }
        } else {
            // (nuevo) HU-10 criterio: sin permiso el mapa sigue funcionando.
            permisoDenegado = true
            viewModel.limpiarRecomendado()
        }
    }

    // (nuevo) Solicita el permiso al abrir la pantalla.
    LaunchedEffect(Unit) {
        val concedido = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (concedido) {
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                client.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        centroUsuario = GeoPoint(it.latitude, it.longitude)
                        viewModel.calcularRecomendado(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) { /* sin ubicación */ }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Puntos de Recojo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {

            // (nuevo) El mapa osmdroid envuelto en AndroidView.
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        setUseDataConnection(true)
                        controller.setZoom(13.0)
                        controller.setCenter(TRUJILLO)
                    }
                },
                update = { mapView ->
                    // (nuevo) Redibuja marcadores cuando cambian los puntos.
                    mapView.overlays.clear()
                    puntos.forEach { punto ->
                        Marker(mapView).apply {
                            position = GeoPoint(punto.latitud, punto.longitud)
                            title = "${punto.tipo}: ${punto.nombre}"
                            snippet = "${punto.direccion} | ${punto.detalle}"
                        }.also { mapView.overlays.add(it) }
                    }
                    // (nuevo) Centra una sola vez en el usuario si hay ubicación.
                    val centro = centroUsuario
                    if (centro != null && !yaCentrado) {
                        mapView.controller.setCenter(centro)
                        yaCentrado = true
                    }
                    mapView.invalidate()
                }
            )

            // (nuevo) Tarjetas superpuestas abajo: aviso de permiso y recomendación.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                if (permisoDenegado) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Sin permiso de ubicación: se muestran todos los puntos de recojo. Puedes acercar el mapa manualmente.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                recomendado?.let { punto ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                text = "Punto recomendado para ti",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${punto.nombre} (${punto.tipo})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "A ${(distancia / 1000f).let { String.format(Locale.US, "%.1f km", it) }} - ${punto.direccion}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (punto.campaignTitulo.isNotBlank()) {
                                Text(
                                    text = "Campaña: ${punto.campaignTitulo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
