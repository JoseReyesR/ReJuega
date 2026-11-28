// (nuevo) Helper del patrón de respaldo: intenta Node, si falla usa el respaldo.
import '../../core/utils/logger.dart';

Future<T> nodeFirst<T>({
  required Future<T> Function() node,
  required Future<T> Function() respaldo,
  String tag = '',
}) async {
  try {
    return await node();
  } catch (e) {
    AppLogger.w('>>> $tag: Node falló ($e) → uso respaldo');
    return await respaldo();
  }
}

// (nuevo) Envoltorio que indica si los datos vinieron de Node o del respaldo.
class Resultado<T> {
  final T data;
  final bool desdeRespaldo;
  const Resultado(this.data, this.desdeRespaldo);
}
