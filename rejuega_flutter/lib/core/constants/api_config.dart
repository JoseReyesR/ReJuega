// (modificado) Timeouts más largos para redes móviles con latencia.
class ApiConfig {
  ApiConfig._();

  // (modificado) URL base del servidor Node.
  static const String nodeBaseUrl = 'http://10.103.185.141:3000/';

  // (modificado) Timeouts aumentados para evitar timeouts en redes lentas.
  static const int connectTimeoutSeconds = 15;    // antes: 3
  static const int receiveTimeoutSeconds = 20;    // antes: 5
  static const int sendTimeoutSeconds = 20;       // antes: 5

  static const String cloudName = 'eg8sugwt';
  static const String uploadPreset = 'rejuega_preset';
}
