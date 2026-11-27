// (nuevo) Galería de evidencias (lectura; escritura en Fase 5).
import 'package:cloud_firestore/cloud_firestore.dart';
import '../models/app_models.dart';
import '../services/node_api.dart';
import 'repo_utils.dart';

class GalleryRepository {
  final NodeApi _api = NodeApi();
  final FirebaseFirestore _fs = FirebaseFirestore.instance;

  Future<List<EvidenciaModel>> obtenerPorCampaign(String campaignId) => nodeFirst(
        tag: 'GALLERY',
        node: () async =>
            (await _api.getGallery(campaignId)).map(EvidenciaModel.fromDto).toList(),
        respaldo: () async {
          final snap = await _fs.collection('campaignGallery')
              .where('campaignId', isEqualTo: campaignId).get();
          return snap.docs.map(EvidenciaModel.fromFirestore).toList();
        },
      );
}
