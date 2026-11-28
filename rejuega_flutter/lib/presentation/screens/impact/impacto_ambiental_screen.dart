// (nuevo) Catálogo de materiales con imagen y CO₂ (como antes en Kotlin).
import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../state/campaign_providers.dart';

class ImpactoAmbientalScreen extends ConsumerWidget {
  const ImpactoAmbientalScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final materialesAsync = ref.watch(materialsProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Impacto Ambiental')),
      body: materialesAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('Error: $e')),
        data: (materiales) => ListView.separated(
          padding: const EdgeInsets.all(16),
          itemCount: materiales.length,
          separatorBuilder: (_, __) => const SizedBox(height: 8),
          itemBuilder: (context, i) {
            final m = materiales[i];
            return Card(
              child: ListTile(
                leading: ClipRRect(
                  borderRadius: BorderRadius.circular(8),
                  child: CachedNetworkImage(
                    imageUrl: m.imagenUrl,
                    width: 56, height: 56, fit: BoxFit.cover,
                    errorWidget: (_, __, ___) =>
                        const Icon(Icons.eco, size: 40),
                  ),
                ),
                title: Text(m.tipoMaterial,
                    style: const TextStyle(fontWeight: FontWeight.w600)),
                subtitle: Text(m.descripcionImpacto),
                trailing: Text('${m.co2AhorradoKg} kg',
                    style: TextStyle(
                        fontWeight: FontWeight.bold,
                        color: Theme.of(context).colorScheme.primary)),
                isThreeLine: true,
              ),
            );
          },
        ),
      ),
    );
  }
}
