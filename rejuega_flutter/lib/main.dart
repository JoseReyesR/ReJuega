// (modificado) Punto de entrada de ReJuega Flutter.
// (modificado) Inicializa Firebase antes de correr la app.
import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'firebase_options.dart';

void main() async {
  // (nuevo) Necesario para usar plugins antes de runApp.
  WidgetsFlutterBinding.ensureInitialized();

  // (nuevo) Inicializa Firebase con las opciones generadas por flutterfire.
  await Firebase.initializeApp(
    options: DefaultFirebaseOptions.currentPlatform,
  );

  runApp(const ReJuegaApp());
}

// (nuevo) Widget raíz de la aplicación.
class ReJuegaApp extends StatelessWidget {
  const ReJuegaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'ReJuega',
      // (nuevo) Tema base verde (identidad de ReJuega).
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF4CAF50)),
        useMaterial3: true,
      ),
      home: const Scaffold(
        body: Center(
          child: Text(
            'ReJuega Flutter 🧸\nFirebase conectado ✅',
            textAlign: TextAlign.center,
            style: TextStyle(fontSize: 20),
          ),
        ),
      ),
    );
  }
}
