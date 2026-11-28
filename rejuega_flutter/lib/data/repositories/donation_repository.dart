// (modificado) Todas las escrituras a Firestore ahora registran OK/FAIL visible.
// (modificado) Editar preserva el historial de trazabilidad de Firestore.
import 'package:cloud_firestore/cloud_firestore.dart';
import '../../core/utils/logger.dart';
import '../models/app_models.dart';
import '../services/node_api.dart';
import 'repo_utils.dart';

class DonationRepository {
  final NodeApi _api = NodeApi();
  final FirebaseFirestore _fs = FirebaseFirestore.instance;

  // ---------- LECTURAS ----------
  Future<List<DonationModel>> obtenerPorUsuario(String uid) => nodeFirst(
        tag: 'DONATIONS',
        node: () async =>
            (await _api.getDonations(userId: uid)).map((d) => DonationModel.fromDto(d)).toList(),
        respaldo: () async {
          final snap = await _fs.collection('donations').where('uid', isEqualTo: uid).get();
          return snap.docs.map(DonationModel.fromFirestore).toList();
        },
      );

  Future<List<DonationModel>> obtenerTodas() => nodeFirst(
        tag: 'DONATIONS_ALL',
        node: () async =>
            (await _api.getDonations()).map((d) => DonationModel.fromDto(d)).toList(),
        respaldo: () async {
          final snap = await _fs.collection('donations').get();
          return snap.docs.map(DonationModel.fromFirestore).toList();
        },
      );

  Future<DonationModel> obtenerPorId(String id) => nodeFirst(
        tag: 'DONATION_DETAIL',
        node: () async => DonationModel.fromDto(await _api.getDonation(id)),
        respaldo: () async {
          final doc = await _fs.collection('donations').doc(id).get();
          if (!doc.exists) throw Exception('Donación no encontrada');
          return DonationModel.fromFirestore(doc);
        },
      );

  // ---------- ESCRITURA FIRESTORE CON LOG (nunca silenciosa) ----------
  // (nuevo) merge:true para no pisar campos que no manejamos.
  Future<void> _writeFirestore(String id, Map<String, dynamic> data) async {
    try {
      await _fs.collection('donations').doc(id).set(data, SetOptions(merge: true));
      AppLogger.i('>>> DONATION FIRESTORE OK: $id');
    } catch (e) {
      // (nuevo) Se ve en consola: aquí aparecía el fallo que antes se tragaba el catch.
      AppLogger.e('>>> DONATION FIRESTORE FAIL: $id → $e');
      rethrow;
    }
  }

  // ---------- REGISTRAR ----------
  Future<void> registrar(DonationModel d) async {
    try {
      await _api.createDonation(d.toDto());
      AppLogger.i('>>> DONATION CREATE OK MySQL: ${d.id}');
    } catch (e) {
      AppLogger.w('>>> DONATION CREATE FAIL MySQL: $e → solo Firestore');
    }
    await _writeFirestore(d.id, d.toFirestoreMap());
  }

  // ---------- EDITAR (preserva historial) ----------
  Future<void> editar(DonationModel d) async {
    // (nuevo) 1) Recuperar el historial real desde Firestore si el local viene vacío.
    List<HistorialEstado> historial = d.historial;
    try {
      final doc = await _fs.collection('donations').doc(d.id).get();
      if (doc.exists && historial.isEmpty) {
        historial = (doc.data()?['historialEstados'] as List<dynamic>? ?? [])
            .map((e) => HistorialEstado.fromMap(Map<String, dynamic>.from(e as Map)))
            .toList();
      }
    } catch (e) {
      AppLogger.w('>>> DONATION EDIT: no pude leer historial previo ($e)');
    }

    final conHistorial = DonationModel(
      id: d.id, uid: d.uid, campaignFirestoreId: d.campaignFirestoreId,
      campaignTitulo: d.campaignTitulo, nombre: d.nombre, descripcion: d.descripcion,
      condicion: d.condicion, fotoUrl: d.fotoUrl,
      estadoTrazabilidad: d.estadoTrazabilidad, motivoRechazo: d.motivoRechazo,
      materialTipo: d.materialTipo, co2AhorradoKg: d.co2AhorradoKg,
      fechaRegistro: d.fechaRegistro, historial: historial,
    );

    // (nuevo) 2) MySQL vía Node (best-effort).
    try {
      await _api.updateDonation(conHistorial.id, conHistorial.toDto());
      AppLogger.i('>>> DONATION UPDATE OK MySQL: ${d.id}');
    } catch (e) {
      AppLogger.w('>>> DONATION UPDATE FAIL MySQL: $e → solo Firestore');
    }

    // (nuevo) 3) Firestore siempre, con error visible si falla.
    await _writeFirestore(conHistorial.id, conHistorial.toFirestoreMap());
  }

  // ---------- CAMBIAR ESTADO (trazabilidad) ----------
  Future<void> actualizarEstado(
    String id,
    String nuevoEstado,
    String cambiadoPor, {
    String? motivo,
  }) async {
    try {
      await _api.updateDonationEstado(id, nuevoEstado, cambiadoPor, motivo: motivo);
      AppLogger.i('>>> DONATION STATE OK MySQL: $id → $nuevoEstado');
    } catch (e) {
      AppLogger.w('>>> DONATION STATE FAIL MySQL: $e');
    }

    // (nuevo) Agregar el paso al historial de Firestore (paridad con MySQL).
    List<HistorialEstado> historial = [];
    try {
      final doc = await _fs.collection('donations').doc(id).get();
      if (doc.exists) {
        historial = (doc.data()?['historialEstados'] as List<dynamic>? ?? [])
            .map((e) => HistorialEstado.fromMap(Map<String, dynamic>.from(e as Map)))
            .toList();
      }
    } catch (_) {}
    historial.add(HistorialEstado(
      estado: nuevoEstado,
      fecha: DateTime.now().millisecondsSinceEpoch,
      cambiadoPor: cambiadoPor,
    ));

    await _fs.collection('donations').doc(id).set({
      'estadoTrazabilidad': nuevoEstado,
      if (motivo != null) 'motivoRechazo': motivo,
      'historialEstados': historial.map((h) => h.toMap()).toList(),
    }, SetOptions(merge: true));
    AppLogger.i('>>> DONATION STATE OK Firestore: $id → $nuevoEstado');
  }

  // ---------- ELIMINAR ----------
  Future<void> eliminar(String id) async {
    try {
      await _api.deleteDonation(id);
      AppLogger.i('>>> DONATION DELETE OK MySQL: $id');
    } catch (e) {
      AppLogger.w('>>> DONATION DELETE FAIL MySQL: $e → solo Firestore');
    }
    try {
      await _fs.collection('donations').doc(id).delete();
      AppLogger.i('>>> DONATION DELETE OK Firestore: $id');
    } catch (e) {
      AppLogger.e('>>> DONATION DELETE FAIL Firestore: $id → $e');
      rethrow;
    }
  }
}
