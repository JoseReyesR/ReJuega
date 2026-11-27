// (nuevo) Menú lateral (equivalente a MenuLateralReJuega.kt).
// (nuevo) Por ahora muestra opciones base; en Fase 4/5 se agregarán todas.
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../core/router/app_router.dart';

class AppDrawer extends StatelessWidget {
  const AppDrawer({super.key});

  @override
  Widget build(BuildContext context) {
    return Drawer(
      child: SafeArea(
        child: Column(
          children: [
            // (nuevo) Cabecera con nombre del usuario (por ahora hardcodeado).
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(20),
              color: Theme.of(context).colorScheme.primaryContainer,
              child: const Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  CircleAvatar(
                    radius: 28,
                    child: Icon(Icons.person, size: 32),
                  ),
                  SizedBox(height: 12),
                  Text(
                    'Usuario de Prueba',
                    style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                  ),
                  Text(
                    'usuario@rejuega.app',
                    style: TextStyle(fontSize: 12),
                  ),
                ],
              ),
            ),
            // (nuevo) Opciones del menú.
            Expanded(
              child: ListView(
                padding: EdgeInsets.zero,
                children: [
                  _drawerItem(context, Icons.home, 'Inicio', AppRoutes.home),
                  _drawerItem(context, Icons.dns, 'Pruebas del Servidor', AppRoutes.test),
                  const Divider(),
                  _drawerItem(context, Icons.person, 'Mi Perfil', '/'),  // TODO: ruta real
                  _drawerItem(context, Icons.notifications, 'Notificaciones', '/'),  // TODO
                  const Divider(),
                  ListTile(
                    leading: const Icon(Icons.logout, color: Colors.red),
                    title: const Text('Cerrar Sesión'),
                    onTap: () {
                      // (nuevo) TODO: implementar logout en Fase 2.
                      Navigator.pop(context);
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Logout pendiente - Fase 2')),
                      );
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

  // (nuevo) Widget helper para cada item del drawer.
  Widget _drawerItem(BuildContext context, IconData icon, String label, String route) {
    return ListTile(
      leading: Icon(icon),
      title: Text(label),
      onTap: () {
        Navigator.pop(context);   // (nuevo) Cierra el drawer.
        context.go(route);        // (nuevo) Navega con go_router.
      },
    );
  }
}
