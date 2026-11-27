// (modificado) Router con guardias de sesión: sin login no se entra a la app.
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../presentation/layouts/shell_layout.dart';
import '../../presentation/screens/home/home_screen.dart';
import '../../presentation/screens/test/connectivity_test_screen.dart';
import '../../presentation/screens/login/login_screen.dart';
import '../../presentation/screens/login/register_screen.dart';
import '../../presentation/screens/splash/splash_screen.dart';
import '../../state/auth_providers.dart';

class AppRoutes {
  AppRoutes._();

  static const String splash = '/splash';
  static const String home = '/';
  static const String login = '/login';
  static const String register = '/register';
  static const String test = '/test';
}

final goRouterProvider = Provider<GoRouter>((ref) {
  final refresh = ref.watch(authRefreshProvider);

  return GoRouter(
    initialLocation: AppRoutes.splash,
    refreshListenable: refresh,
    debugLogDiagnostics: true,
    redirect: (context, state) {
      // (fix) Lectura SINCRÓNICA del usuario persistido.
      // (fix) Ya NO dependemos del AsyncValue de Riverpod: eso trababa el splash.
      final user = FirebaseAuth.instance.currentUser;
      final loc = state.matchedLocation;

      const publicRoutes = {AppRoutes.login, AppRoutes.register};

      // (fix) Diagnóstico: quítalo cuando confirmes que funciona.
      debugPrint('>>> ROUTER: loc=$loc user=${user?.email}');

      // (fix) Sin sesión: solo se permiten login/registro.
      if (user == null) {
        return publicRoutes.contains(loc) ? null : AppRoutes.login;
      }

      // (fix) Con sesión: nadie se queda en splash/login/registro.
      if (loc == AppRoutes.splash || publicRoutes.contains(loc)) {
        return AppRoutes.home;
      }

      return null;
    },
    routes: [
      GoRoute(
        path: AppRoutes.splash,
        name: 'splash',
        builder: (context, state) => const SplashScreen(),
      ),
      GoRoute(
        path: AppRoutes.login,
        name: 'login',
        builder: (context, state) => const LoginScreen(),
      ),
      GoRoute(
        path: AppRoutes.register,
        name: 'register',
        builder: (context, state) => const RegisterScreen(),
      ),
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
        ],
      ),
    ],
    errorBuilder: (context, state) => Scaffold(
      body: Center(child: Text('Ruta no encontrada: ${state.uri}')),
    ),
  );
});
