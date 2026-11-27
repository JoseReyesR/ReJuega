// (modificado) Usa goRouterProvider (con guardias) en lugar del router estático.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'core/router/app_router.dart';
import 'core/theme/app_theme.dart';

class ReJuegaApp extends ConsumerWidget {
  const ReJuegaApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return MaterialApp.router(
      title: 'ReJuega',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      routerConfig: ref.watch(goRouterProvider),
    );
 //   return ProviderScope(
 //     child: MaterialApp.router(
  //      title: 'ReJuega',
  //      theme: AppTheme.lightTheme,
  //      routerConfig: ref.watch(goRouterProvider),
  //    ),
 //   );
  }
}
