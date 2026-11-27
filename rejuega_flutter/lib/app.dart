// (nuevo) Widget raíz de la app.
// (nuevo) Envuelve todo con ProviderScope (Riverpod) y MaterialApp.router (go_router).
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'core/router/app_router.dart';
import 'core/theme/app_theme.dart';

class ReJuegaApp extends StatelessWidget {
  const ReJuegaApp({super.key});

  @override
  Widget build(BuildContext context) {
    // (nuevo) ProviderScope habilita Riverpod en todo el árbol de widgets.
    return ProviderScope(
      child: MaterialApp.router(
        title: 'ReJuega',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.lightTheme,
        routerConfig: goRouter,
      ),
    );
  }
}
