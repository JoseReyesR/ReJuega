// (nuevo) Puntos fijos y eventos de recojo (lectura; escritura llega en Fase 5).
import 'package:cloud_firestore/cloud_firestore.dart';
import '../models/app_models.dart';
import '../services/node_api.dart';
import 'repo_utils.dart';

class PuntoRecojoRepository {
  final NodeApi _api = NodeApi();
  final FirebaseFirestore _fs = FirebaseFirestore.instance;

  // ⚠️ (nuevo) Verifica en tu Firestore Console el nombre exacto de la colección.
  // (nuevo) Si ves 'pickupevents' (todo minúsculas), cambia esta constante.
  static const String colEventos = 'pickupEvents';

  Future<List<PuntoEntregaModel>> obtenerPuntosActivos() => nodeFirst(
        tag: 'PUNTOS',
        node: () async => (await _api.getDeliveryPoints())
            .map(PuntoEntregaModel.fromDto)
            .where((p) => p.activo)
            .toList(),
        respaldo: () async {
          final snap = await _fs.collection('deliveryPoints').where('activo', isEqualTo: true).get();
          return snap.docs.map(PuntoEntregaModel.fromFirestore).toList();
        },
      );

  Future<List<PickupEventModel>> obtenerEventosPorCampaign(String campaignId) => nodeFirst(
        tag: 'EVENTOS',
        node: () async => (await _api.getPickupEvents(campaignId: campaignId))
            .map(PickupEventModel.fromDto)
            .toList(),
        respaldo: () async {
          final snap = await _fs.collection(colEventos)
              .where('campaignId', isEqualTo: campaignId).get();
          return snap.docs.map(PickupEventModel.fromFirestore).toList();
        },
      );
}
