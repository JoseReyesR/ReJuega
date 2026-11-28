// (modificado) Shell con acciones superiores: huella, campana con badge y logout.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../core/router/app_router.dart';
import '../../state/auth_providers.dart';
import '../../state/campaign_providers.dart';
import '../widgets/app_drawer.dart';

class ShellLayout extends ConsumerWidget {
  final Widget child;
  const ShellLayout({super.key, required this.child});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final uid = ref.watch(currentUserProvider).value?.uid;
    final unread = uid == null ? 0 : (ref.watch(unreadCountProvider(uid)).value ?? 0);

    return Scaffold(
      appBar: AppBar(
        title: const Text('ReJuega', style: TextStyle(fontWeight: FontWeight.bold)),
        leading: Builder(
          builder: (context) => IconButton(
            icon: const Icon(Icons.menu),
            onPressed: () => Scaffold.of(context).openDrawer(),
          ),
        ),
        actions: [
          // (nuevo) Huella ecológica rápida.
          IconButton(
            icon: const Icon(Icons.eco),
            tooltip: 'Mi Huella Ecológica',
            onPressed: () => context.go(AppRoutes.huella),
          ),
          // (nuevo) Campana con badge de no leídas.
          IconButton(
            icon: Badge(
              isLabelVisible: unread > 0,
              label: Text('$unread'),
              child: const Icon(Icons.notifications),
            ),
            tooltip: 'Notificaciones',
            onPressed: () => context.go(AppRoutes.notificaciones),
          ),
          // (nuevo) Logout.
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Cerrar sesión',
            onPressed: () => ref.read(authServiceProvider).signOut(),
          ),
        ],
      ),
      drawer: const AppDrawer(),
      body: child,
    );
  }
}
