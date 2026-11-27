// (nuevo) Constantes de configuración de la API (equivalente a ApiConfig.kt).
class ApiConfig {
  ApiConfig._();

  // (nuevo) URL base del servidor Node.
  // (nuevo) Cambia esto según el entorno:
  //   - Emulador Android: http://10.0.2.2:3000/
  //   - Celular físico:   http://TU_IP_LOCAL:3000/
  //   - Producción:       https://tu-dominio.com/
  static const String nodeBaseUrl = 'http://192.168.100.106:3000/';


  // (nuevo) Cloudinary.
  static const String cloudName = 'eg8sugwt';   // reemplaza nombre del cloud name:
  static const String uploadPreset = 'rejuega_preset';

  // (nuevo) Timeouts (en segundos) — los mismos que usamos en Kotlin (3s).
  static const int connectTimeoutSeconds = 3;
  static const int receiveTimeoutSeconds = 5;
  static const int sendTimeoutSeconds = 5;
}
