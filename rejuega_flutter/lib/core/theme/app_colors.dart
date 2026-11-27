// (nuevo) Paleta de colores de ReJuega.
// (nuevo) Centraliza todos los colores usados en la app para consistencia.
import 'package:flutter/material.dart';

class AppColors {
  AppColors._();   // (nuevo) Constructor privado: no se instancia.

  // (nuevo) Color principal verde (identidad de ReJuega).
  static const Color primary = Color(0xFF4CAF50);
  static const Color primaryDark = Color(0xFF388E3C);
  static const Color primaryLight = Color(0xFFC8E6C9);

  // (nuevo) Color secundario (acentos).
  static const Color secondary = Color(0xFFFFA726);

  // (nuevo) Colores de estado (como en Kotlin).
  static const Color success = Color(0xFF2E7D32);
  static const Color warning = Color(0xFFEF6C00);
  static const Color error = Color(0xFFC62828);
  static const Color info = Color(0xFF1976D2);

  // (nuevo) Colores para los roles (usados en Gestión de Usuarios).
  static const Color rolAdministrador = Color(0xFFC62828);
  static const Color rolGestor = Color(0xFFEF6C00);
  static const Color rolDonante = Color(0xFF2E7D32);

  // (nuevo) Helper: devuelve el color correspondiente a un rol.
  static Color colorForRol(String rol) {
    switch (rol) {
      case 'Administrador': return rolAdministrador;
      case 'Gestor': return rolGestor;
      default: return rolDonante;
    }
  }
}
