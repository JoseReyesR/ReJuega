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

  // (modificado) Intenta Node hasta 2 veces antes de caer al respaldo.
  // (modificado) Resuelve el "banner falso" del primer arranque.
  Future<Resultado<List<CampaignModel>>> obtenerConOrigen() async {
    // (nuevo) Helper local para un intento de lectura desde Node.
    Future<List<CampaignModel>> intentoNode() async {
      final dtos = await _api.getCampaigns();
      return dtos.map(CampaignModel.fromDto).toList();
    }

    // (nuevo) Intento 1
    try {
      final lista = await intentoNode();
      print('>>> CAMPAIGNS: Node OK (intento 1), ${lista.length} campañas');
      return Resultado(lista, false);
    } catch (e1) {
      print('>>> CAMPAIGNS: Node falló en intento 1 ($e1) → reintentando en 1s');

      // (nuevo) Espera 1 segundo y reintenta (la red suele estabilizarse).
      await Future.delayed(const Duration(seconds: 1));

      try {
        final lista = await intentoNode();
        print('>>> CAMPAIGNS: Node OK (intento 2), ${lista.length} campañas');
        return Resultado(lista, false);
      } catch (e2) {
        // (nuevo) Ambos intentos fallaron → cae al respaldo Firestore.
        print('>>> CAMPAIGNS: Node falló 2 veces ($e2) → uso Firestore');
        try {
          final snap = await _fs.collection('campaigns').get();
          final lista = snap.docs.map(CampaignModel.fromFirestore).toList();
          return Resultado(lista, true);
        } catch (e3) {
          print('>>> CAMPAIGNS: Firestore también falló ($e3)');
          return Resultado([], true);
        }
      }
    }
  }

  // (nuevo) Detalle de una campaña por su firestoreId.
  Future<CampaignModel> obtenerPorId(String id) => nodeFirst(
        tag: 'CAMPAIGN_DETAIL',
        node: () async => CampaignModel.fromDto(await _api.getCampaign(id)),
        respaldo: () async {
          final doc = await _fs.collection('campaigns').doc(id).get();
          if (!doc.exists) throw Exception('Campaña no encontrada');
          return CampaignModel.fromFirestore(doc);
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
