// (modificado) Home real: lista de campañas con filtro, banner offline y tarjetas.
import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/router/app_router.dart';
import '../../../core/utils/date_utils.dart';
import '../../../data/models/app_models.dart';
import '../../../state/campaign_providers.dart';

class HomeScreen extends ConsumerStatefulWidget {
  const HomeScreen({super.key});
  @override
  ConsumerState<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends ConsumerState<HomeScreen> {
  String _filtro = 'Activas';

  // (nuevo) Al entrar al Home, fuerza un refetch para asegurar datos frescos.
  // (nuevo) Soluciona el caso raro en que el primer fetch fue con la red inestable.
  @override
  void initState() {
    super.initState();
    Future.delayed(const Duration(milliseconds: 600), () {
      if (mounted) {
        ref.invalidate(campaignsProvider);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final resultadoAsync = ref.watch(campaignsProvider);

    return Column(
      children: [
        // (nuevo) Banner de modo respaldo (equivalente a estaSincronizado de Kotlin).
        resultadoAsync.maybeWhen(
          data: (r) {
            if (!r.desdeRespaldo) return const SizedBox.shrink();
            return Container(
              width: double.infinity,
              color: Theme.of(context).colorScheme.secondaryContainer,
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: Row(
                children: [
                  Icon(Icons.wifi_off,
                      size: 18,
                      color: Theme.of(context).colorScheme.onSecondaryContainer),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      'Sin conexión con el servidor. Mostrando campañas guardadas.',
                      style: TextStyle(
                          fontSize: 12,
                          color: Theme.of(context).colorScheme.onSecondaryContainer),
                    ),
                  ),
                ],
              ),
            );
          },
          orElse: () => const SizedBox.shrink(),
        ),

        // (nuevo) Chips de filtro Activas / Cerradas / Todas.
        SizedBox(
          height: 48,
          child: ListView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 16),
            children: ['Activas', 'Cerradas', 'Todas'].map((op) {
              return Padding(
                padding: const EdgeInsets.only(right: 8),
                child: FilterChip(
                  label: Text(op),
                  selected: _filtro == op,
                  onSelected: (_) => setState(() => _filtro = op),
                ),
              );
            }).toList(),
          ),
        ),

        // (nuevo) Lista según estado.
        Expanded(
          child: resultadoAsync.when(
            loading: () => const Center(child: CircularProgressIndicator()),
            error: (e, _) => Center(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Text('Error al cargar campañas:\n$e',
                    textAlign: TextAlign.center),
              ),
            ),
            data: (resultado) {
              // (nuevo) Filtrar según el chip seleccionado
              final campanasFiltradas = resultado.data.where((c) {
                switch (_filtro) {
                  case 'Activas':
                    return c.estado == 'Activa';
                  case 'Cerradas':
                    return c.estado == 'Cerrada';
                  case 'Todas':
                  default:
                    return true;
                }
              }).toList();

              if (campanasFiltradas.isEmpty) {
                return Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.event_busy, size: 48, color: Colors.grey),
                      const SizedBox(height: 8),
                      Text('No hay campañas $_filtro disponibles',
                          textAlign: TextAlign.center),
                    ],
                  ),
                );
              }

              return RefreshIndicator(
                onRefresh: () async => ref.invalidate(campaignsProvider),
                child: ListView.separated(
                  padding: const EdgeInsets.all(16),
                  itemCount: campanasFiltradas.length,
                  separatorBuilder: (_, __) => const SizedBox(height: 16),
                  itemBuilder: (context, i) => _CampaignCard(
                    campaign: campanasFiltradas[i],
                  ),
                ),
              );
            },
          ),
        ),
      ],
    );
  }
}

// (nuevo) Tarjeta de campaña equivalente a CampaignCard de Compose.
class _CampaignCard extends StatelessWidget {
  final CampaignModel campaign;
  const _CampaignCard({required this.campaign});

  @override
  Widget build(BuildContext context) {
    final activa = campaign.estado == 'Activa';
    return Card(
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: () => context.push(
          AppRoutes.campaignDetail.replaceFirst(':id', campaign.id),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // (nuevo) Imagen de la campaña.
            CachedNetworkImage(
              imageUrl: campaign.imagenUrl,
              height: 180,
              fit: BoxFit.cover,
              placeholder: (_, __) => Container(
                height: 180,
                color: Theme.of(context).colorScheme.surfaceContainerHighest,
                child: const Center(child: CircularProgressIndicator()),
              ),
              errorWidget: (_, __, ___) => Container(
                height: 180,
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
                      style: Theme.of(context).textTheme.titleLarge),
                  const SizedBox(height: 4),
                  Text(campaign.descripcion,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: Theme.of(context).textTheme.bodyMedium),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      const Icon(Icons.date_range, size: 16),
                      const SizedBox(width: 4),
                      Text(
                        '${AppDateUtils.fechaVisible(campaign.fechaInicio)} → '
                        '${AppDateUtils.fechaVisible(campaign.fechaFin)}',
                        style: Theme.of(context).textTheme.bodySmall,
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      Container(
                        width: 8,
                        height: 8,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: activa ? Colors.green : Colors.red,
                        ),
                      ),
                      const SizedBox(width: 6),
                      Text(campaign.estado,
                          style: TextStyle(
                            fontWeight: FontWeight.w500,
                            color: activa ? Colors.green : Colors.red,
                          )),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
