// (nuevo) Notificaciones: Node primario, Firestore respaldo.
import 'package:cloud_firestore/cloud_firestore.dart';
import '../models/app_models.dart';
import '../dtos/node_dtos.dart';
import '../services/node_api.dart';
import 'repo_utils.dart';

class NotificationRepository {
  final NodeApi _api = NodeApi();
  final FirebaseFirestore _fs = FirebaseFirestore.instance;

  Future<List<NotificacionModel>> obtenerPorUsuario(String uid) => nodeFirst(
        tag: 'NOTIFS',
        node: () async =>
            (await _api.getNotifications(uid)).map(NotificacionModel.fromDto).toList(),
        respaldo: () async {
          final snap = await _fs
              .collection('notifications')
              .where('uidDestinatario', isEqualTo: uid)
              .get();
          return snap.docs.map(NotificacionModel.fromFirestore).toList();
        },
      );

  Future<int> contarNoLeidas(String uid) => nodeFirst(
        tag: 'NOTIFS_COUNT',
        node: () => _api.getUnreadCount(uid),
        respaldo: () async {
          final snap = await _fs
              .collection('notifications')
              .where('uidDestinatario', isEqualTo: uid)
              .where('leida', isEqualTo: false)
              .get();
          return snap.size;
        },
      );

  Future<void> marcarLeida(String id) async {
    try {
      await _api.markRead(id);
      try { await _fs.collection('notifications').doc(id).update({'leida': true}); } catch (_) {}
    } catch (e) {
      await _fs.collection('notifications').doc(id).update({'leida': true});
    }
  }

  Future<void> marcarTodasLeidas(String uid) async {
    try {
      await _api.markAllRead(uid);
    } catch (e) {
      final snap = await _fs.collection('notifications')
          .where('uidDestinatario', isEqualTo: uid)
          .where('leida', isEqualTo: false).get();
      for (final doc in snap.docs) {
        await _fs.collection('notifications').doc(doc.id).update({'leida': true});
      }
    }
  }

  // (nuevo) Crear notificación (la usará el admin en Fase 5).
  Future<void> crear(NotificacionModel n) async {
    try {
      await _api.createNotification(NotificationNodeDto(
        id: n.id, userId: n.uidDestinatario, titulo: n.titulo, mensaje: n.mensaje,
        donationId: n.donacionId, tipo: n.tipo, leida: n.leida,
        fechaCreacion: n.fechaCreacion,
      ));
      try { await _fs.collection('notifications').doc(n.id).set(n.toFirestoreMap()); } catch (_) {}
    } catch (e) {
      await _fs.collection('notifications').doc(n.id).set(n.toFirestoreMap());
    }
  }
}
