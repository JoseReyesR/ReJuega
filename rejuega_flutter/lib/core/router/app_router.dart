// (nuevo) Configuración central de rutas (equivalente a Routes.kt + AppNavigation.kt).
// (nuevo) Usa go_router para navegación declarativa con guardias.
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../presentation/layouts/shell_layout.dart';
import '../../presentation/screens/home/home_screen.dart';
import '../../presentation/screens/test/connectivity_test_screen.dart';
import '../../presentation/screens/login/login_screen.dart';

// (nuevo) Constantes de rutas (equivalente a Routes.kt).
class AppRoutes {
  AppRoutes._();

  static const String home = '/';
  static const String login = '/login';
  static const String test = '/test';

  // (nuevo) Agrega aquí las demás rutas a medida que migres pantallas:
  // static const String campaignDetail = '/campaign/:firestoreId';
  // static const String profile = '/profile';
  // ...
}

// (nuevo) Instancia del router con todas las rutas.
final goRouter = GoRouter(
  initialLocation: AppRoutes.home,
  debugLogDiagnostics: true,   // (nuevo) Logs de navegación en debug.
  routes: [
    // (nuevo) Layout Shell (con drawer) envuelve las pantallas principales.
    ShellRoute(
      builder: (context, state, child) => ShellLayout(child: child),
      routes: [
        GoRoute(
          path: AppRoutes.home,
          name: 'home',
          builder: (context, state) => const HomeScreen(),
        ),
        GoRoute(
          path: AppRoutes.test,
          name: 'test',
          builder: (context, state) => const ConnectivityTestScreen(),
        ),
        // (nuevo) Aquí agregaremos más rutas en siguientes fases.
      ],
    ),
    // (nuevo) Rutas sin drawer (login, etc).
    GoRoute(
      path: AppRoutes.login,
      name: 'login',
      builder: (context, state) => const LoginScreen(),
    ),
  ],
  // (nuevo) Pantalla de error para rutas inexistentes.
  errorBuilder: (context, state) => Scaffold(
    body: Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.error_outline, size: 64, color: Colors.red),
          const SizedBox(height: 16),
          Text('Ruta no encontrada: ${state.uri}'),
        ],
      ),
    ),
  ),
);
