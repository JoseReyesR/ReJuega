// (nuevo) Providers globales de autenticación y sesión.
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/models/user_model.dart';
import '../data/services/auth_service.dart';

// (nuevo) Instancia única del servicio.
final authServiceProvider = Provider<AuthService>((ref) => AuthService());

// (nuevo) Estado de sesión de Firebase (loading / con usuario / sin usuario).
final authStateProvider = StreamProvider<User?>((ref) {
  return ref.watch(authServiceProvider).authStateChanges;
});

// (nuevo) Perfil completo del usuario actual (con rol en vivo desde Firestore).
final currentUserProvider = StreamProvider<UserModel?>((ref) {
  final auth = ref.watch(authStateProvider);

  // (nuevo) Mientras carga o sin sesión, no emitimos perfil.
  if (auth.isLoading) return Stream.value(null);
  final user = auth.value;
  if (user == null) return Stream.value(null);

  // (nuevo) Stream del documento de perfil: si un Admin cambia el rol,
  // (nuevo) esta pantalla se actualiza sola sin reiniciar.
  return ref.watch(authServiceProvider).profileStream(user.uid);
});

// (nuevo) Notificador para que go_router re-evalúe las guardias al cambiar la sesión.
class AuthRefreshNotifier extends ChangeNotifier {
  AuthRefreshNotifier(AuthService service) {
    service.authStateChanges.listen((_) => notifyListeners());
  }
}

final authRefreshProvider = Provider<AuthRefreshNotifier>((ref) {
  final notifier = AuthRefreshNotifier(ref.watch(authServiceProvider));
  ref.onDispose(notifier.dispose);
  return notifier;
});
