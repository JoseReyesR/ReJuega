// (nuevo) Mis Donaciones: lista con foto, estado, historial expandible
// (nuevo) y editar/eliminar SOLO en estado Pendiente (regla del servidor).
import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/router/app_router.dart';
import '../../../core/utils/date_utils.dart';
import '../../../data/models/app_models.dart';
import '../../../state/auth_providers.dart';
import '../../../state/campaign_providers.dart';
import '../../../state/data_providers.dart';

class MyDonationsScreen extends ConsumerWidget {
  const MyDonationsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final uid = ref.watch(currentUserProvider).value?.uid ?? '';
    final listaAsync = ref.watch(myDonationsProvider(uid));

    return Scaffold(
      appBar: AppBar(title: const Text('Mis Donaciones')),
      body: listaAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Padding(
          padding: const EdgeInsets.all(24),
          child: Text('Error al cargar tus donaciones:\n$e',
              textAlign: TextAlign.center),
        )),
        data: (lista) {
          if (lista.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.volunteer_activism,
                      size: 64,
                      color: Theme.of(context).colorScheme.onSurfaceVariant),
                  const SizedBox(height: 8),
                  const Text('Aún no has donado juguetes.\n¡Explora las campañas!'),
                ],
              ),
            );
          }

          // (nuevo) Más recientes primero.
          final ordenadas = [...lista]
            ..sort((a, b) => b.fechaRegistro.compareTo(a.fechaRegistro));

          return RefreshIndicator(
            onRefresh: () async => ref.invalidate(myDonationsProvider),
            child: ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: ordenadas.length,
              separatorBuilder: (_, __) => const SizedBox(height: 12),
              itemBuilder: (context, i) =>
                  _DonationCard(donacion: ordenadas[i]),
            ),
          );
        },
      ),
    );
  }
}

// (nuevo) Tarjeta individual con expansión de historial y acciones.
class _DonationCard extends ConsumerStatefulWidget {
  final DonationModel donacion;
  const _DonationCard({required this.donacion});

  @override
  ConsumerState<_DonationCard> createState() => _DonationCardState();
}

class _DonationCardState extends ConsumerState<_DonationCard> {
  bool _expanded = false;
  List<HistorialEstado>? _historial;
  bool _loadingHist = false;

  // (nuevo) Al expandir: usa el historial local o lo trae del detalle.
  Future<void> _toggle() async {
    final newState = !_expanded;
    setState(() => _expanded = newState);

    if (newState && _historial == null) {
      if (widget.donacion.historial.isNotEmpty) {
        setState(() => _historial = widget.donacion.historial);
        return;
      }
      setState(() => _loadingHist = true);
      try {
        final detalle = await ref
            .read(donationRepositoryProvider)
            .obtenerPorId(widget.donacion.id);
        setState(() => _historial = detalle.historial);
      } catch (e) {
        setState(() => _historial = []);
      }
      if (mounted) setState(() => _loadingHist = false);
    }
  }

  // (nuevo) Eliminar con confirmación (solo Pendiente).
  Future<void> _eliminar() async {
    final confirmar = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Eliminar donación'),
        content: Text(
            '¿Seguro que deseas eliminar "${widget.donacion.nombre}"? Esta acción no se puede deshacer.'),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: const Text('Cancelar')),
          TextButton(
              onPressed: () => Navigator.pop(ctx, true),
              child: const Text('Eliminar',
                  style: TextStyle(color: Colors.red))),
        ],
      ),
    );

    if (confirmar != true) return;

    try {
      await ref.read(donationRepositoryProvider).eliminar(widget.donacion.id);
      ref.invalidate(myDonationsProvider);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Donación eliminada')),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error al eliminar: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final d = widget.donacion;
    final esPendiente = d.estadoTrazabilidad == 'Pendiente';

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                // (nuevo) Miniatura de la foto.
                ClipRRect(
                  borderRadius: BorderRadius.circular(8),
                  child: CachedNetworkImage(
                    imageUrl: d.fotoUrl,
                    width: 64, height: 64, fit: BoxFit.cover,
                    errorWidget: (_, __, ___) =>
                        const Icon(Icons.toys, size: 40),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(d.nombre,
                          style: const TextStyle(
                              fontWeight: FontWeight.bold, fontSize: 16)),
                      Text(d.campaignTitulo,
                          style: Theme.of(context).textTheme.bodySmall),
                      Text(
                        'Registrada: ${AppDateUtils.fechaHora(d.fechaRegistro)}',
                        style: Theme.of(context).textTheme.bodySmall),
                      Text('${d.co2AhorradoKg.toStringAsFixed(2)} kg CO₂ · ${d.materialTipo}',
                          style: Theme.of(context).textTheme.bodySmall),
                    ],
                  ),
                ),
                _chipEstado(d.estadoTrazabilidad),
              ],
            ),

            const SizedBox(height: 8),

            // (nuevo) Botón de historial expandible.
            InkWell(
              onTap: _toggle,
              child: Row(
                children: [
                  Icon(_expanded
                      ? Icons.expand_less
                      : Icons.expand_more, size: 20),
                  const Text('Ver trazabilidad',
                      style: TextStyle(fontSize: 13)),
                ],
              ),
            ),

            // (nuevo) Historial de estados.
            if (_expanded) ...[
              const Divider(),
              if (_loadingHist)
                const LinearProgressIndicator()
              else if (_historial == null || _historial!.isEmpty)
                const Text('Sin historial disponible.',
                    style: TextStyle(fontSize: 12))
              else
                ..._historial!.map((h) => Padding(
                      padding: const EdgeInsets.only(bottom: 6),
                      child: Row(
                        children: [
                          Icon(Icons.circle, size: 8, color: _colorEstado(h.estado)),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(h.estado,
                                style: const TextStyle(fontSize: 13,
                                    fontWeight: FontWeight.w500)),
                          ),
                          Text(AppDateUtils.fechaHora(h.fecha),
                              style: const TextStyle(fontSize: 11,
                                  color: Colors.grey)),
                        ],
                      ),
                    )),
            ],

            // (nuevo) Acciones SOLO en Pendiente.
            if (esPendiente) ...[
              const Divider(),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: () => context.push(
                        AppRoutes.editarDonacion
                            .replaceFirst(':id', d.id),
                        extra: d,
                      ),
                      icon: const Icon(Icons.edit, size: 18),
                      label: const Text('Editar'),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: _eliminar,
                      style: OutlinedButton.styleFrom(
                          foregroundColor: Colors.red),
                      icon: const Icon(Icons.delete, size: 18),
                      label: const Text('Eliminar'),
                    ),
                  ),
                ],
              ),
            ],
          ],
        ),
      ),
    );
  }

  // (nuevo) Chip de estado con color semántico.
  Widget _chipEstado(String estado) {
    final c = _colorEstado(estado);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: c.withValues(alpha: 0.15),
        borderRadius: BorderRadius.circular(10),
      ),
      child: Text(estado,
          style: TextStyle(color: c, fontSize: 11, fontWeight: FontWeight.w600)),
    );
  }

  Color _colorEstado(String estado) {
    switch (estado) {
      case 'Pendiente': return Colors.orange;
      case 'En Revision': return Colors.blue;
      case 'Aprobado': return Colors.green;
      case 'Entregado': return Colors.teal;
      case 'Rechazado': return Colors.red;
      default: return Colors.grey;
    }
  }
}
