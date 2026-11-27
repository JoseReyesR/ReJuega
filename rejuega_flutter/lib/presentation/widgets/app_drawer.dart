// (modificado) Drawer con usuario real de Firestore y menú según rol.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../core/router/app_router.dart';
import '../../core/theme/app_colors.dart';
import '../../state/auth_providers.dart';

class AppDrawer extends ConsumerWidget {
  const AppDrawer({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final profileAsync = ref.watch(currentUserProvider);
    final profile = profileAsync.value;

    return Drawer(
      child: SafeArea(
        child: Column(
          children: [
            // (nuevo) Cabecera con datos reales del usuario y chip de rol.
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(20),
              color: Theme.of(context).colorScheme.primaryContainer,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  CircleAvatar(
                    radius: 28,
                    child: Text(
                      (profile?.nombre.isNotEmpty == true)
                          ? profile!.nombre[0].toUpperCase()
                          : '?',
                      style: const TextStyle(fontSize: 24),
                    ),
                  ),
                  const SizedBox(height: 12),
                  Text(
                    profile?.nombre ?? 'Cargando...',
                    style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                  ),
                  Text(
                    profile?.correo ?? '',
                    style: const TextStyle(fontSize: 12),
                  ),
                  const SizedBox(height: 8),
                  // (nuevo) Chip con el rol y su color.
                  if (profile != null)
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: AppColors.colorForRol(profile.rol),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Text(
                        profile.rol,
                        style: const TextStyle(color: Colors.white, fontSize: 12),
                      ),
                    ),
                ],
              ),
            ),

            Expanded(
              child: ListView(
                padding: EdgeInsets.zero,
                children: [
                  _item(context, Icons.home, 'Inicio', AppRoutes.home),
                  _item(context, Icons.dns, 'Pruebas del Servidor', AppRoutes.test),
                  const Divider(),

                  // (nuevo) Sección de gestión: solo Gestor y Administrador.
                  if (profile?.esGestorOAdmin == true) ...[
                    const Padding(
                      padding: EdgeInsets.fromLTRB(16, 8, 16, 4),
                      child: Text('GESTIÓN', style: TextStyle(fontSize: 12, color: Colors.grey)),
                    ),
                    ListTile(
                      leading: const Icon(Icons.campaign),
                      title: const Text('Gestión de Campañas'),
                      onTap: () => _pendiente(context, 'Fase 5'),
                    ),
                    ListTile(
                      leading: const Icon(Icons.volunteer_activism),
                      title: const Text('Revisar Donaciones'),
                      onTap: () => _pendiente(context, 'Fase 5'),
                    ),
                    // (nuevo) Gestión de usuarios: SOLO Administrador.
                    if (profile?.esAdministrador == true)
                      ListTile(
                        leading: const Icon(Icons.people),
                        title: const Text('Gestión de Usuarios'),
                        onTap: () => _pendiente(context, 'Fase 5'),
                      ),
                    const Divider(),
                  ],

                  // (nuevo) Sección personal: todos los roles.
                  const Padding(
                    padding: EdgeInsets.fromLTRB(16, 8, 16, 4),
                    child: Text('MI ESPACIO', style: TextStyle(fontSize: 12, color: Colors.grey)),
                  ),
                  ListTile(
                    leading: const Icon(Icons.person),
                    title: const Text('Mi Perfil'),
                    onTap: () => _pendiente(context, 'Fase 4'),
                  ),
                  ListTile(
                    leading: const Icon(Icons.notifications),
                    title: const Text('Notificaciones'),
                    onTap: () => _pendiente(context, 'Fase 4'),
                  ),
                  const Divider(),
                  ListTile(
                    leading: const Icon(Icons.logout, color: Colors.red),
                    title: const Text('Cerrar Sesión', style: TextStyle(color: Colors.red)),
                    onTap: () async {
                      Navigator.pop(context);   // (nuevo) Cierra el drawer.
                      await ref.read(authServiceProvider).signOut();
                      // (nuevo) El redirect del router lleva al login solo.
                    },
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _item(BuildContext context, IconData icon, String label, String route) {
    return ListTile(
      leading: Icon(icon),
      title: Text(label),
      onTap: () {
        Navigator.pop(context);
        context.go(route);
      },
    );
  }

  // (nuevo) Snackbar temporal para pantallas aún no migradas.
  void _pendiente(BuildContext context, String fase) {
    Navigator.pop(context);
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Disponible en la $fase de la migración')),
    );
  }
}
