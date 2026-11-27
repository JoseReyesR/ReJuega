// (nuevo) Pantalla que valida cada repositorio: Node primario y respaldo.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/utils/date_utils.dart';
import '../../../state/data_providers.dart';
import '../../../state/auth_providers.dart';

class DataLayerTestScreen extends ConsumerStatefulWidget {
  const DataLayerTestScreen({super.key});
  @override
  ConsumerState<DataLayerTestScreen> createState() => _DataLayerTestScreenState();
}

class _DataLayerTestScreenState extends ConsumerState<DataLayerTestScreen> {
  final List<String> _log = [];

  void _add(String msg) => setState(() => _log.insert(0, '• $msg'));

  Future<void> _probar(String nombre, Future<List<String>> Function() fn) async {
    _add('⏳ $nombre...');
    try {
      final resultados = await fn();
      _add('✅ $nombre: ${resultados.length} registros');
      for (final r in resultados.take(3)) {
        _add('   └ $r');
      }
    } catch (e) {
      _add('❌ $nombre falló: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    final uid = ref.watch(currentUserProvider).value?.uid ?? '';

    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              FilledButton.tonal(
                onPressed: () => _probar('Campañas', () async {
                  final lista = await ref.read(campaignRepositoryProvider).obtenerCampaigns();
                  return lista.map((c) => '${c.titulo} (${c.estado}) ${AppDateUtils.fechaVisible(c.fechaInicio)}').toList();
                }),
                child: const Text('Campañas'),
              ),
              FilledButton.tonal(
                onPressed: uid.isEmpty ? null : () => _probar('Mis donaciones', () async {
                  final lista = await ref.read(donationRepositoryProvider).obtenerPorUsuario(uid);
                  return lista.map((d) => '${d.nombre} → ${d.estadoTrazabilidad} ${AppDateUtils.fechaHora(d.fechaRegistro)}').toList();
                }),
                child: const Text('Mis donaciones'),
              ),
              FilledButton.tonal(
                onPressed: () => _probar('Materiales', () async {
                  final lista = await ref.read(materialRepositoryProvider).obtenerMateriales();
                  return lista.map((m) => '${m.tipoMaterial}: ${m.co2AhorradoKg} kg').toList();
                }),
                child: const Text('Materiales'),
              ),
              FilledButton.tonal(
                onPressed: uid.isEmpty ? null : () async {
                  final n = await ref.read(notificationRepositoryProvider).contarNoLeidas(uid);
                  _add('✅ No leídas: $n');
                },
                child: const Text('No leídas'),
              ),
              FilledButton.tonal(
                onPressed: () => _probar('Puntos de recojo', () async {
                  final lista = await ref.read(puntoRecojoRepositoryProvider).obtenerPuntosActivos();
                  return lista.map((p) => '${p.nombre} (${p.dias.join("/")})').toList();
                }),
                child: const Text('Puntos'),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Expanded(
            child: Card(
              child: ListView(
                padding: const EdgeInsets.all(12),
                children: _log
                    .map((l) => Text(l, style: const TextStyle(fontFamily: 'monospace', fontSize: 12)))
                    .toList(),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
