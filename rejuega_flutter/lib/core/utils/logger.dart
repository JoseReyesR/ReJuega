// (nuevo) Logger centralizado.
// (nuevo) En release se pueden desactivar los logs sin tocar el código.
import 'dart:developer' as developer;
import 'package:flutter/foundation.dart';

class AppLogger {
  AppLogger._();

  static void d(String message) {
    if (kDebugMode) developer.log(message, name: 'ReJuega');
  }

  static void i(String message) {
    if (kDebugMode) developer.log('ℹ️  $message', name: 'ReJuega');
  }

  static void w(String message) {
    if (kDebugMode) developer.log('⚠️  $message', name: 'ReJuega');
  }

  static void e(String message, [Object? error]) {
    if (kDebugMode) developer.log('❌ $message $error', name: 'ReJuega');
  }
}
