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
        data: (notificaciones) {
          if (notificaciones.isEmpty) {
            return const Center(
              child: Text('No tienes notificaciones'),
            );
          }

          // (nuevo) Ordenar por fecha descendente
          final ordenadas = [...notificaciones]
            ..sort((a, b) => b.fechaCreacion.compareTo(a.fechaCreacion));

          return ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: ordenadas.length,
            itemBuilder: (context, index) {
              final notif = ordenadas[index];
              return Card(
                margin: const EdgeInsets.only(bottom: 8),
                child: ListTile(
                  leading: Icon(
                    notif.leida
                        ? Icons.notifications_none
                        : Icons.notifications_active,
                    color: notif.leida ? Colors.grey : Colors.blue,
                  ),
                  title: Text(
                    notif.titulo,
                    style: TextStyle(
                      fontWeight: notif.leida ? FontWeight.normal : FontWeight.bold,
                    ),
                  ),
                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(notif.mensaje),
                      Text(
                        AppDateUtils.fechaHora(notif.fechaCreacion),
                        style: const TextStyle(fontSize: 12, color: Colors.grey),
                      ),
                    ],
                  ),
                  trailing: notif.leida
                      ? null
                      : IconButton(
                          icon: const Icon(Icons.check_circle_outline),
                          onPressed: () async {
                            await ref
                                .read(notificationRepositoryProvider)
                                .marcarLeida(notif.id);
                            ref.invalidate(myNotificationsProvider);
                            ref.invalidate(unreadCountProvider);
                          },
                        ),
                  onTap: notif.leida
                      ? null
                      : () async {
                          await ref
                              .read(notificationRepositoryProvider)
                              .marcarLeida(notif.id);
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
