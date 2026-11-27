// (nuevo) Materiales: Node primario, semilla local como respaldo (no están en Firestore).
import '../models/app_models.dart';
import '../services/node_api.dart';
import 'repo_utils.dart';

class MaterialRepository {
  final NodeApi _api = NodeApi();

  Future<List<MaterialModel>> obtenerMateriales() => nodeFirst(
        tag: 'MATERIALES',
        node: () async =>
            (await _api.getMateriales()).map(MaterialModel.fromDto).toList(),
        respaldo: () async => MaterialModel.seed,
      );
}
