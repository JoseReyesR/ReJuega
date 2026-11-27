// (nuevo) Repositorio de campañas: Node primario, Firestore respaldo.
import 'package:cloud_firestore/cloud_firestore.dart';
import '../../core/utils/logger.dart';
import '../models/app_models.dart';
import '../services/node_api.dart';
import 'repo_utils.dart';

class CampaignRepository {
  final NodeApi _api = NodeApi();
  final FirebaseFirestore _fs = FirebaseFirestore.instance;

  // (nuevo) Lectura con respaldo.
  Future<List<CampaignModel>> obtenerCampaigns() => nodeFirst(
        tag: 'CAMPAIGNS',
        node: () async =>
            (await _api.getCampaigns()).map(CampaignModel.fromDto).toList(),
        respaldo: () async {
          final snap = await _fs.collection('campaigns').get();
          return snap.docs.map(CampaignModel.fromFirestore).toList();
        },
      );

  // (nuevo) Escritura: Node + copia Firestore; si Node cae, solo Firestore.
  Future<void> crearCampaign(CampaignModel c) async {
    try {
      await _api.createCampaign(c.toDto());
      try {
        await _fs.collection('campaigns').doc(c.id).set(c.toFirestoreMap());
      } catch (_) {}
    } catch (e) {
      AppLogger.w('>>> CAMPAIGN CREATE FAIL: $e → uso Firestore');
      await _fs.collection('campaigns').doc(c.id).set(c.toFirestoreMap());
    }
  }

  Future<void> actualizarCampaign(CampaignModel c) async {
    try {
      await _api.updateCampaign(c.id, c.toDto());
      try {
        await _fs.collection('campaigns').doc(c.id).set(c.toFirestoreMap());
      } catch (_) {}
    } catch (e) {
      await _fs.collection('campaigns').doc(c.id).set(c.toFirestoreMap());
    }
  }

  Future<void> cambiarEstado(String id, String estado) async {
    try {
      await _api.updateCampaignEstado(id, estado);
      try {
        await _fs.collection('campaigns').doc(id).update({'estado': estado});
      } catch (_) {}
    } catch (e) {
      await _fs.collection('campaigns').doc(id).update({'estado': estado});
    }
  }
}
