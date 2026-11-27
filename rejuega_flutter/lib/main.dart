// (modificado) Entry point simplificado: solo inicializa Firebase y arranca la app.
import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';   // (nuevo)
import 'firebase_options.dart';
import 'app.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(
    options: DefaultFirebaseOptions.currentPlatform,
  );
  // (modificado) ProviderScope envuelve a ReJuegaApp para que su ref funcione.
  runApp(const ProviderScope(child: ReJuegaApp()));
}
