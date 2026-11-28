// (nuevo) Huella ecológica con cálculo real (solo Aprobado/Entregado) y desglose.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../state/auth_providers.dart';
import '../../../state/campaign_providers.dart';

class MiHuellaEcologicaScreen extends ConsumerWidget {
  const MiHuellaEcologicaScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final uid = ref.watch(currentUserProvider).value?.uid ?? '';
    final donacionesAsync = ref.watch(myDonationsProvider(uid));

    return Scaffold(
      appBar: AppBar(title: const Text('Mi Huella Ecológica')),
      body: donacionesAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('Error: $e')),
        data: (donaciones) {
          // (nuevo) Mismo cálculo que Kotlin: solo Aprobado/Entregado suman.
          final validas = donaciones
              .where((d) =>
                  d.estadoTrazabilidad == 'Aprobado' ||
                  d.estadoTrazabilidad == 'Entregado')
              .toList();
          final co2 = validas.fold<double>(0, (s, d) => s + d.co2AhorradoKg);

          final pendientes = donaciones.where((d) => d.estadoTrazabilidad == 'Pendiente').length;
          final enRevision = donaciones.where((d) => d.estadoTrazabilidad == 'En Revision').length;
          final aprobados = donaciones.where((d) => d.estadoTrazabilidad == 'Aprobado').length;
          final entregados = donaciones.where((d) => d.estadoTrazabilidad == 'Entregado').length;
          final rechazados = donaciones.where((d) => d.estadoTrazabilidad == 'Rechazado').length;

          final arboles = co2 / 22.0;
          final kmAuto = co2 / 0.21;

          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              const Icon(Icons.eco, size: 80, color: Colors.green),
              const SizedBox(height: 8),
              Center(
                child: Text('Tu impacto acumulado',
                    style: Theme.of(context).textTheme.headlineSmall),
              ),
              const SizedBox(height: 24),
              _tarjeta(context, '${validas.length}',
                  validas.length == 1 ? 'juguete donado con éxito' : 'juguetes donados con éxito',
                  Colors.green.shade50, Colors.green.shade800),
              const SizedBox(height: 16),
              _tarjeta(context, co2.toStringAsFixed(2), 'kg de CO₂ evitados',
                  Colors.orange.shade50, Colors.orange.shade800),
              const SizedBox(height: 16),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Estado de tus donaciones',
                          style: Theme.of(context).textTheme.titleSmall),
                      const SizedBox(height: 8),
                      Text('Pendientes: $pendientes    En revisión: $enRevision'),
                      Text('Aprobados: $aprobados    Entregados: $entregados'),
                      Text('Rechazados: $rechazados'),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Center(
                child: Text('Tu aporte equivale a:',
                    style: Theme.of(context).textTheme.titleMedium),
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  Expanded(child: _tarjeta(context, arboles.toStringAsFixed(1), 'árboles en 1 año', Colors.green.shade50, Colors.green.shade700)),
                  const SizedBox(width: 12),
                  Expanded(child: _tarjeta(context, kmAuto.toStringAsFixed(0), 'km en auto evitados', Colors.green.shade50, Colors.green.shade700)),
                ],
              ),
            ],
          );
        },
      ),
    );
  }

  Widget _tarjeta(BuildContext context, String valor, String etiqueta,
      Color fondo, Color texto) {
    return Card(
      color: fondo,
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            Text(valor,
                textAlign: TextAlign.center,
                style: Theme.of(context)
                    .textTheme
                    .displayMedium
                    ?.copyWith(color: texto, fontWeight: FontWeight.bold)),
            Text(etiqueta,
                textAlign: TextAlign.center,
                style: TextStyle(color: texto)),
          ],
        ),
      ),
    );
  }
}
