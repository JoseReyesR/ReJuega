// (nuevo) Modelo de usuario equivalente al Usuario de Kotlin.
// (nuevo) Se lee/escribe en Firestore colección 'users'.
import 'package:cloud_firestore/cloud_firestore.dart';

class UserModel {
  final String uid;
  final String nombre;
  final String correo;
  final String rol;
  final String direccion;
  final String telefono;
  final String ciudad;
  final String genero;

  const UserModel({
    required this.uid,
    required this.nombre,
    required this.correo,
    this.rol = 'Donante',
    this.direccion = '',
    this.telefono = '',
    this.ciudad = '',
    this.genero = '',
  });

  // (nuevo) Construye desde un DocumentSnapshot de Firestore.
  factory UserModel.fromFirestore(DocumentSnapshot doc) {
    final data = doc.data() as Map<String, dynamic>? ?? {};
    return UserModel(
      uid: doc.id,
      nombre: (data['nombre'] as String?) ?? '',
      correo: (data['correo'] as String?) ?? '',
      rol: (data['rol'] as String?) ?? 'Donante',
      direccion: (data['direccion'] as String?) ?? '',
      telefono: (data['telefono'] as String?) ?? '',
      ciudad: (data['ciudad'] as String?) ?? '',
      genero: (data['genero'] as String?) ?? '',
    );
  }

  // (nuevo) Mapa para escribir en Firestore (mismos campos que Kotlin).
  Map<String, dynamic> toMap() => {
        'uid': uid,
        'nombre': nombre,
        'correo': correo,
        'rol': rol,
        'direccion': direccion,
        'telefono': telefono,
        'ciudad': ciudad,
        'genero': genero,
      };

  // (nuevo) Helpers de rol (equivalentes a esAdministrador / esGestorOAdmin).
  bool get esAdministrador => rol == 'Administrador';
  bool get esGestor => rol == 'Gestor';
  bool get esGestorOAdmin => esGestor || esAdministrador;
}
