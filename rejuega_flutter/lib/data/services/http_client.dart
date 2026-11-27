// (nuevo) Cliente HTTP configurado con dio.
// (nuevo) Se expone como un Provider singleton en Riverpod.
import 'package:dio/dio.dart';
import '../../core/constants/api_config.dart';
import '../../core/utils/logger.dart';

class HttpClient {
  // (nuevo) Instancia única de Dio configurada.
  static final Dio instance = Dio(
    BaseOptions(
      baseUrl: ApiConfig.nodeBaseUrl,
      connectTimeout: Duration(seconds: ApiConfig.connectTimeoutSeconds),
      receiveTimeout: Duration(seconds: ApiConfig.receiveTimeoutSeconds),
      sendTimeout: Duration(seconds: ApiConfig.sendTimeoutSeconds),
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    ),
  )..interceptors.add(_LoggingInterceptor());   // (nuevo) Logs de todas las peticiones.
}

// (nuevo) Interceptor que loggea cada request/response (equivalente a println de Kotlin).
class _LoggingInterceptor extends Interceptor {
  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    AppLogger.d('>>> HTTP ${options.method} ${options.uri}');
    if (options.data != null) {
      AppLogger.d('>>> BODY: ${options.data}');
    }
    handler.next(options);
  }

  @override
  void onResponse(Response response, ResponseInterceptorHandler handler) {
    AppLogger.d('<<< HTTP ${response.statusCode} ${response.requestOptions.uri}');
    handler.next(response);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    AppLogger.e('!!! HTTP ERROR ${err.type}: ${err.message}');
    handler.next(err);
  }
}
