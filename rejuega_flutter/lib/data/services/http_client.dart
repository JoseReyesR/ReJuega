// (nuevo) Cliente HTTP configurado con dio.
// (nuevo) Se expone como un Provider singleton en Riverpod.
import 'package:dio/dio.dart';
import '../../core/constants/api_config.dart';

class HttpClient {
  static final Dio _dio = Dio(
    BaseOptions(
      connectTimeout: Duration(seconds: ApiConfig.connectTimeoutSeconds),
      receiveTimeout: Duration(seconds: ApiConfig.receiveTimeoutSeconds),
      sendTimeout: Duration(seconds: ApiConfig.sendTimeoutSeconds),
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    ),
  )..interceptors.add(_LoggingInterceptor());

  // (nuevo) Propiedad getter que siempre actualiza baseUrl desde ApiConfig.nodeBaseUrl.
  static Dio get instance {
    _dio.options.baseUrl = ApiConfig.nodeBaseUrl;
    return _dio;
  }
}

// (modificado) Interceptor con print() directo para garantizar visibilidad.
class _LoggingInterceptor extends Interceptor {
  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    print('🔵 >>> HTTP ${options.method} ${options.uri}');
    handler.next(options);
  }

  @override
  void onResponse(Response response, ResponseInterceptorHandler handler) {
    print('🟢 <<< HTTP ${response.statusCode} ${response.requestOptions.uri}');
    handler.next(response);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    print('🔴 !!! HTTP ERROR ${err.type}: ${err.message}');
    print('🔴 !!! URL: ${err.requestOptions.uri}');
    handler.next(err);
  }
}
