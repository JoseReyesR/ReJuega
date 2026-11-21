// (nuevo) Pantalla independiente de Mi Huella Ecológica.
// (nuevo) Muestra el CO₂ real ahorrado con base en donaciones APROBADAS/ENTREGADAS y desglose por estado.
package pe.rejuega.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.rejuega.app.viewmodel.MiHuellaEcologicaViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiHuellaEcologicaScreen(
    viewModel: MiHuellaEcologicaViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Huella Ecológica", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.recalcular() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recalcular")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    Column(
                        Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // (nuevo) Ícono ecológico grande.
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tu impacto acumulado",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))

                        // (nuevo) Tarjeta principal: cantidad de juguetes donados válidamente.
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    state.donacionesValidas.toString(),
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    if (state.donacionesValidas == 1) "juguete donado con éxito"
                                    else "juguetes donados con éxito",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // (nuevo) Tarjeta secundaria: CO₂ total evitado.
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    String.format(Locale.US, "%.2f", state.co2TotalKg),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    "kg de CO₂ evitados",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // (nuevo) Desglose por estado de tus donaciones.
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "Estado de tus donaciones",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth()) {
                                    Text("Pendientes: ${state.pendientes}", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                    Text("En revisión: ${state.enRevision}", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(Modifier.fillMaxWidth()) {
                                    Text("Aprobados: ${state.aprobados}", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                    Text("Entregados: ${state.entregados}", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("Rechazados: ${state.rechazados}", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // (nuevo) Explicación del cálculo.
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "¿Cómo se calcula?",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "• Solo se cuentan juguetes Aprobados o Entregados.\n" +
                                    "• Los Pendientes, En Revisión y Rechazados no suman.\n" +
                                    "• El CO₂ se toma del material real de cada juguete donado.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // (nuevo) Equivalencias ilustrativas.
                        Text(
                            "Tu aporte equivale a:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))

                        val arbolesEquivalentes = state.co2TotalKg / 22.0   // 1 árbol ≈ 22 kg CO₂/año
                        val kmAutoEquivalentes = state.co2TotalKg / 0.21    // 1 km auto ≈ 0.21 kg CO₂

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(Modifier.weight(1f)) {
                                Column(
                                    Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        String.format(Locale.US, "%.1f", arbolesEquivalentes),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("árboles en 1 año", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                                }
                            }
                            Card(Modifier.weight(1f)) {
                                Column(
                                    Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        String.format(Locale.US, "%.0f", kmAutoEquivalentes),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("km en auto evitados", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
