// (nuevo) Centro de notificaciones con marcado como leída.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/utils/date_utils.dart';
import '../../../state/auth_providers.dart';
import '../../../state/campaign_providers.dart';
import '../../../state/data_providers.dart';

class NotificationScreen extends ConsumerWidget {
  const NotificationScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final uid = ref.watch(currentUserProvider).value?.uid ?? '';
    final listaAsync = ref.watch(myNotificationsProvider(uid));

    return Scaffold(
      appBar: AppBar(
        title: const Text('Notificaciones'),
        actions: [
          IconButton(
            icon: const Icon(Icons.done_all),
            tooltip: 'Marcar todas como leídas',
            onPressed: () async {
              await ref.read(notificationRepositoryProvider).marcarTodasLeidas(uid);
              ref.invalidate(myNotificationsProvider);
              ref.invalidate(unreadCountProvider);
            },
          ),
        ],
      ),
      body: listaAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('Error: $e')),
        data: (lista) {
          if (lista.isEmpty) {
            return const Center(child: Text('No tienes notificaciones aún.'));
          }
          // (nuevo) Más recientes primero.
          final ordenadas = [...lista]
            ..sort((a, b) => b.fechaCreacion.compareTo(a.fechaCreacion));

          return ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: ordenadas.length,
            separatorBuilder: (_, __) => const SizedBox(height: 8),
            itemBuilder: (context, i) {
              final n = ordenadas[i];
              return Card(
                color: n.leida
                    ? null
                    : Theme.of(context).colorScheme.primaryContainer.withValues(alpha: 0.4),
                child: ListTile(
                  leading: Icon(
                    n.leida ? Icons.notifications_outlined : Icons.notifications_active,
                    color: n.leida ? Colors.grey : Theme.of(context).colorScheme.primary,
                  ),
                  title: Text(n.titulo,
                      style: TextStyle(
                          fontWeight: n.leida ? FontWeight.normal : FontWeight.bold)),
                  subtitle: Text(
                    '${n.mensaje}\n${AppDateUtils.fechaHora(n.fechaCreacion)}',
                  ),
                  isThreeLine: true,
                  onTap: n.leida
                      ? null
                      : () async {
                          await ref.read(notificationRepositoryProvider).marcarLeida(n.id);
                          ref.invalidate(myNotificationsProvider);
                          ref.invalidate(unreadCountProvider);
                        },
                ),
              );
            },
          );
        },
      ),
    );
  }
}
