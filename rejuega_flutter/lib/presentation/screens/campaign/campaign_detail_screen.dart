// (nuevo) Detalle de campaña con eventos de recojo y acción según rol.
import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/router/app_router.dart';
import '../../../core/utils/date_utils.dart';
import '../../../state/auth_providers.dart';
import '../../../state/campaign_providers.dart';

class CampaignDetailScreen extends ConsumerWidget {
  final String campaignId;
  const CampaignDetailScreen({super.key, required this.campaignId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final campaignAsync = ref.watch(campaignByIdProvider(campaignId));
    final eventsAsync = ref.watch(eventsByCampaignProvider(campaignId));
    final perfil = ref.watch(currentUserProvider).value;

    return Scaffold(
      appBar: AppBar(title: const Text('Detalle de campaña')),
      body: campaignAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Padding(
          padding: const EdgeInsets.all(24),
          child: Text('Error: $e', textAlign: TextAlign.center),
        )),
        data: (campaign) {
          final activa = campaign.estado == 'Activa';
          final esGestorOAdmin = perfil?.esGestorOAdmin ?? false;

          return ListView(
            padding: EdgeInsets.zero,
            children: [
              // (nuevo) Imagen principal.
              CachedNetworkImage(
                imageUrl: campaign.imagenUrl,
                height: 220,
                fit: BoxFit.cover,
                errorWidget: (_, __, ___) => Container(
                  height: 220,
                  color: Theme.of(context).colorScheme.surfaceContainerHighest,
                  child: const Icon(Icons.image_not_supported, size: 48),
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(campaign.titulo,
                        style: Theme.of(context).textTheme.headlineSmall),
                    const SizedBox(height: 8),
                    Text('Estado: ${campaign.estado}',
                        style: TextStyle(
                          fontWeight: FontWeight.w600,
                          color: activa ? Colors.green : Colors.red,
                        )),
                    const SizedBox(height: 8),
                    Row(
                      children: [
                        const Icon(Icons.date_range, size: 16),
                        const SizedBox(width: 4),
                        Text(
                          '${AppDateUtils.fechaVisible(campaign.fechaInicio)} → '
                          '${AppDateUtils.fechaVisible(campaign.fechaFin)}',
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    Text('Descripción',
                        style: Theme.of(context).textTheme.titleMedium),
                    const SizedBox(height: 4),
                    Text(campaign.descripcion,
                        style: Theme.of(context).textTheme.bodyLarge),
                    const SizedBox(height: 16),

                    // (nuevo) Eventos de recojo programados.
                    Text('Puntos de recojo programados',
                        style: Theme.of(context).textTheme.titleMedium),
                    const SizedBox(height: 8),
                    eventsAsync.when(
                      loading: () => const LinearProgressIndicator(),
                      error: (_, __) => const Text('No se pudieron cargar los eventos.'),
                      data: (eventos) {
                        if (eventos.isEmpty) {
                          return const Text('Esta campaña aún no tiene eventos programados.');
                        }
                        return Column(
                          children: eventos
                              .map((e) => Card(
                                    child: ListTile(
                                      leading: const Icon(Icons.event),
                                      title: Text(e.nombre),
                                      subtitle: Text(
                                        '${e.lugar} - ${e.direccion}\n'
                                        '${AppDateUtils.fechaVisible(e.fecha)} a las ${e.hora}',
                                      ),
                                      isThreeLine: true,
                                    ),
                                  ))
                              .toList(),
                        );
                      },
                    ),
                    const SizedBox(height: 24),

                    // (nuevo) Acciones según rol.
                    if (esGestorOAdmin)
                      Row(
                        children: [
                          Expanded(
                            child: OutlinedButton(
                              onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(content: Text('Edición de campañas: Fase 5')),
                              ),
                              child: const Text('Editar'),
                            ),
                          ),
                          const SizedBox(width: 8),
                          Expanded(
                            child: OutlinedButton(
                              onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(content: Text('Galería de evidencias: Fase 5')),
                              ),
                              child: const Text('Galería'),
                            ),
                          ),
                        ],
                      )
                    else ...[
                      FilledButton(
                        onPressed: activa
                            ? () => context.go(
                                  '/donar/${campaign.id}/${Uri.encodeComponent(campaign.titulo)}',
                                )
                            : null,
                        child: const Text('Donar juguete'),
                      ),
                      if (!activa) ...[
                        const SizedBox(height: 8),
                        Text(
                          'Esta campaña ya no admite nuevas donaciones.',
                          textAlign: TextAlign.center,
                          style: TextStyle(color: Theme.of(context).colorScheme.error),
                        ),
                      ],
                    ],
                  ],
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}
