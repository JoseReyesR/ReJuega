// (nuevo) Pantalla de pruebas del servidor (equivalente a ApiTestScreen.kt).
// (nuevo) Verifica que Flutter puede hablar con tu Node.js.
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import '../../../core/constants/api_config.dart';
import '../../../data/services/http_client.dart';

class ConnectivityTestScreen extends StatefulWidget {
  const ConnectivityTestScreen({super.key});

  @override
  State<ConnectivityTestScreen> createState() => _ConnectivityTestScreenState();
}

class _ConnectivityTestScreenState extends State<ConnectivityTestScreen> {
  String _status = 'Presiona el botón para probar';
  Color _statusColor = Colors.grey;
  bool _isLoading = false;

  // (nuevo) Prueba GET /api/materiales y muestra el resultado.
  Future<void> _testConnection() async {
    setState(() {
      _isLoading = true;
      _status = 'Probando conexión a ${ApiConfig.nodeBaseUrl}...';
      _statusColor = Colors.orange;
    });

    try {
      final response = await HttpClient.instance.get('/api/materiales');

      if (response.statusCode == 200) {
        final data = response.data;
        final count = data is Map ? (data['count'] ?? 0) : 0;

        setState(() {
          _status = '✅ Conectado correctamente\n\nMateriales en el servidor: $count\nStatus: ${response.statusCode}';
          _statusColor = Colors.green;
        });
      } else {
        setState(() {
          _status = '⚠️ Respuesta inesperada: ${response.statusCode}';
          _statusColor = Colors.orange;
        });
      }
    } on DioException catch (e) {
      setState(() {
        _status = '❌ Error de conexión\n\nTipo: ${e.type}\nMensaje: ${e.message}\n\n¿El servidor Node está corriendo?\n¿La IP/URL es correcta?';
        _statusColor = Colors.red;
      });
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Configuración',
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                  const SizedBox(height: 8),
                  Text('URL base: ${ApiConfig.nodeBaseUrl}'),
                  Text('Timeout: ${ApiConfig.connectTimeoutSeconds}s'),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          FilledButton.icon(
            onPressed: _isLoading ? null : _testConnection,
            icon: _isLoading
                ? const SizedBox(
                    width: 18, height: 18,
                    child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                  )
                : const Icon(Icons.play_arrow),
            label: Text(_isLoading ? 'Probando...' : 'Probar conexión'),
          ),
          const SizedBox(height: 16),
          Expanded(
            child: Card(
              color: _statusColor.withValues(alpha: 0.1),
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Text(
                  _status,
                  style: TextStyle(
                    color: _statusColor,
                    fontSize: 14,
                    fontFamily: 'monospace',
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
