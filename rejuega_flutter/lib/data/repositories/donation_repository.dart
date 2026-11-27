// (nuevo) Repositorio de donaciones con trazabilidad.
import 'package:cloud_firestore/cloud_firestore.dart';
import '../../core/utils/logger.dart';
import '../models/app_models.dart';
import '../services/node_api.dart';
import 'repo_utils.dart';

class DonationRepository {
  final NodeApi _api = NodeApi();
  final FirebaseFirestore _fs = FirebaseFirestore.instance;

  // (nuevo) Donaciones de un donante (para trazabilidad y huella).
  Future<List<DonationModel>> obtenerPorUsuario(String uid) => nodeFirst(
        tag: 'DONATIONS',
        node: () async =>
            (await _api.getDonations(userId: uid)).map((d) => DonationModel.fromDto(d)).toList(),
        respaldo: () async {
          final snap = await _fs.collection('donations').where('uid', isEqualTo: uid).get();
          return snap.docs.map(DonationModel.fromFirestore).toList();
        },
      );

  // (nuevo) Todas (para revisión del admin en Fase 5).
  Future<List<DonationModel>> obtenerTodas() => nodeFirst(
        tag: 'DONATIONS_ALL',
        node: () async =>
            (await _api.getDonations()).map((d) => DonationModel.fromDto(d)).toList(),
        respaldo: () async {
          final snap = await _fs.collection('donations').get();
          return snap.docs.map(DonationModel.fromFirestore).toList();
        },
      );

  // (nuevo) Registrar: Node primario + copia Firestore.
  Future<void> registrar(DonationModel d) async {
    try {
      await _api.createDonation(d.toDto());
      try {
        await _fs.collection('donations').doc(d.id).set(d.toFirestoreMap());
      } catch (_) {}
    } catch (e) {
      AppLogger.w('>>> DONATION CREATE FAIL: $e → uso Firestore');
      await _fs.collection('donations').doc(d.id).set(d.toFirestoreMap());
    }
  }

  // (nuevo) Avanzar/rechazar estado (trazabilidad).
  Future<void> actualizarEstado(
    String id,
    String nuevoEstado,
    String cambiadoPor, {
    String? motivo,
  }) async {
    try {
      await _api.updateDonationEstado(id, nuevoEstado, cambiadoPor, motivo: motivo);
      try {
        await _fs.collection('donations').doc(id).update({
          'estadoTrazabilidad': nuevoEstado,
          if (motivo != null) 'motivoRechazo': motivo,
        });
      } catch (_) {}
    } catch (e) {
      await _fs.collection('donations').doc(id).update({
        'estadoTrazabilidad': nuevoEstado,
        if (motivo != null) 'motivoRechazo': motivo,
      });
    }
  }

  // (nuevo) Editar/eliminar solo Pendiente (regla del servidor).
  Future<void> editar(DonationModel d) async {
    try {
      await _api.updateDonation(d.id, d.toDto());
      try {
        await _fs.collection('donations').doc(d.id).set(d.toFirestoreMap());
      } catch (_) {}
    } catch (e) {
      await _fs.collection('donations').doc(d.id).set(d.toFirestoreMap());
    }
  }

  Future<void> eliminar(String id) async {
    try {
      await _api.deleteDonation(id);
      try {
        await _fs.collection('donations').doc(id).delete();
      } catch (_) {}
    } catch (e) {
      await _fs.collection('donations').doc(id).delete();
    }
  }
}
