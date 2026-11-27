// (nuevo) Layout base con drawer lateral (equivalente al ModalNavigationDrawer de Kotlin).
// (nuevo) Envuelve todas las pantallas que deben mostrar el menú lateral.
import 'package:flutter/material.dart';
import '../widgets/app_drawer.dart';

class ShellLayout extends StatelessWidget {
  final Widget child;

  const ShellLayout({super.key, required this.child});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'ReJuega',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        leading: Builder(
          // (nuevo) Builder para tener un context con Scaffold disponible.
          builder: (context) => IconButton(
            icon: const Icon(Icons.menu),
            onPressed: () => Scaffold.of(context).openDrawer(),
            tooltip: 'Abrir menú',
          ),
        ),
      ),
      drawer: const AppDrawer(),
      body: child,
    );
  }
}
