// (nuevo) Envoltorio de Firebase Auth + perfil Firestore + sync a Node.
// (nuevo) Equivalente a AuthRepository.kt de Kotlin.
import 'package:firebase_auth/firebase_auth.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import '../../core/utils/logger.dart';
import '../models/user_model.dart';
import 'http_client.dart';

class AuthService {
  final FirebaseAuth _auth = FirebaseAuth.instance;
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;

  // (nuevo) Stream de cambios de sesión (login/logout automáticos).
  Stream<User?> get authStateChanges => _auth.authStateChanges();

  // (nuevo) Usuario Firebase actual (null si no hay sesión).
  User? get currentUser => _auth.currentUser;

  // ---------- LOGIN ----------
  // (nuevo) Inicia sesión y sincroniza el perfil a MySQL (best-effort).
  Future<UserModel> signIn({required String correo, required String password}) async {
    final credential = await _auth.signInWithEmailAndPassword(
      email: correo.trim(),
      password: password,
    );
    final user = credential.user!;

    // (nuevo) Carga (o crea si no existe) el perfil en Firestore.
    final profile = await loadOrCreateProfile(user);

    // (nuevo) Sincroniza a MySQL sin bloquear el login.
    syncToNode(profile);

    return profile;
  }

  // ---------- REGISTRO ----------
  // (nuevo) Crea cuenta en Auth + perfil Firestore (rol Donante) + sync a Node.
  Future<UserModel> register({
    required String nombre,
    required String correo,
    required String password,
  }) async {
    final credential = await _auth.createUserWithEmailAndPassword(
      email: correo.trim(),
      password: password,
    );
    final user = credential.user!;

    // (nuevo) Perfil nuevo con rol Donante (igual que Kotlin).
    final profile = UserModel(
      uid: user.uid,
      nombre: nombre.trim(),
      correo: correo.trim(),
      rol: 'Donante',
    );

    await _firestore.collection('users').doc(user.uid).set({
      ...profile.toMap(),
      'fechaRegistro': _nowSql(),   // (nuevo) mismo formato string que Kotlin
    });

    syncToNode(profile);
    return profile;
  }

  // ---------- PERFIL ----------
  // (nuevo) Lee el perfil de Firestore; si no existe lo crea como Donante.
  Future<UserModel> loadOrCreateProfile(User user) async {
    final doc = await _firestore.collection('users').doc(user.uid).get();

    if (doc.exists) {
      return UserModel.fromFirestore(doc);
    }

    // (nuevo) Perfil inexistente: se crea con rol Donante.
    final profile = UserModel(
      uid: user.uid,
      nombre: user.displayName ?? user.email?.split('@').first ?? 'Usuario',
      correo: user.email ?? '',
      rol: 'Donante',
    );
    await _firestore.collection('users').doc(user.uid).set({
      ...profile.toMap(),
      'fechaRegistro': _nowSql(),
    });
    return profile;
  }

  // (nuevo) Stream del perfil: permite que un cambio de rol se vea en vivo.
  Stream<UserModel?> profileStream(String uid) {
    return _firestore.collection('users').doc(uid).snapshots().map((doc) {
      if (!doc.exists) return null;
      return UserModel.fromFirestore(doc);
    });
  }

  // (modificado) Con logs visibles y set(merge) en Firestore.
  Future<void> actualizarPerfil(UserModel p) async {
    // (nuevo) 1) MySQL vía Node (best-effort).
    try {
      await HttpClient.instance.put('/api/users/${p.uid}', data: {
        'nombre': p.nombre,
        'telefono': p.telefono,
        'ciudad': p.ciudad,
        'direccion': p.direccion,
        'genero': p.genero,
      });
      AppLogger.i('>>> PROFILE UPDATE OK MySQL: ${p.uid}');
    } catch (e) {
      AppLogger.w('>>> PROFILE UPDATE FAIL MySQL: $e → solo Firestore');
    }

    // (nuevo) 2) Firestore siempre, con error visible.
    try {
      await _firestore.collection('users').doc(p.uid).set({
        'nombre': p.nombre,
        'telefono': p.telefono,
        'ciudad': p.ciudad,
        'direccion': p.direccion,
        'genero': p.genero,
      }, SetOptions(merge: true));
      AppLogger.i('>>> PROFILE UPDATE OK Firestore: ${p.uid}');
    } catch (e) {
      AppLogger.e('>>> PROFILE UPDATE FAIL Firestore: ${p.uid} → $e');
      rethrow;
    }
  }

  // ---------- SYNC A NODE ----------
  // (nuevo) POST /api/users/sync — best-effort, nunca lanza excepción.
  void syncToNode(UserModel profile) {
    HttpClient.instance
        .post('/api/users/sync', data: {
      'firebase_uid': profile.uid,
      'nombre': profile.nombre,
      'correo': profile.correo,
      'rol': profile.rol,
    })
        .then((resp) {
      AppLogger.i('>>> USER SYNC OK: ${profile.uid} (HTTP ${resp.statusCode})');
    }).catchError((e) {
      // (nuevo) Si Node está caído, NO fallamos: quedará por auto-reconciliación.
      AppLogger.w('>>> USER SYNC FAIL (server caído?): $e');
    });
  }

  // ---------- LOGOUT ----------
  Future<void> signOut() => _auth.signOut();

  // (nuevo) Fecha en formato 'yyyy-MM-dd HH:mm:ss' (igual que MySQL/Kotlin).
  String _nowSql() {
    final n = DateTime.now();
    String two(int v) => v.toString().padLeft(2, '0');
    return '${n.year}-${two(n.month)}-${two(n.day)} '
        '${two(n.hour)}:${two(n.minute)}:${two(n.second)}';
  }
}
