// (modificado) Logger simple con print() para que siempre aparezca en la consola.
import 'package:flutter/foundation.dart';

class AppLogger {
  AppLogger._();

  static void d(String message) {
    if (kDebugMode) print('🔵 [ReJuega] $message');
  }

  static void i(String message) {
    if (kDebugMode) print('🟢 [ReJuega] $message');
  }

  static void w(String message) {
    if (kDebugMode) print('🟡 [ReJuega] $message');
  }

  static void e(String message, [Object? error]) {
    if (kDebugMode) print('🔴 [ReJuega] $message ${error ?? ""}');
  }
}
