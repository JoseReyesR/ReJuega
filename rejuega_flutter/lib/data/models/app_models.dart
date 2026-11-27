// (nuevo) Modelos de dominio equivalentes a los de Kotlin.
// (nuevo) Cada modelo sabe construirse desde un DTO de Node y desde Firestore.
import 'package:cloud_firestore/cloud_firestore.dart';
import '../dtos/node_dtos.dart';
import '../dtos/parse_utils.dart';

// ---------- CAMPAÑA ----------
class CampaignModel {
  final String id;
  final String titulo;
  final String descripcion;
  final String fechaInicio;
  final String fechaFin;
  final String estado;
  final String imagenUrl;
  final String creadoPor;
  final int fechaCreacion;

  const CampaignModel({
    required this.id, required this.titulo, required this.descripcion,
    required this.fechaInicio, required this.fechaFin, required this.estado,
    this.imagenUrl = '', this.creadoPor = '', this.fechaCreacion = 0,
  });

  factory CampaignModel.fromDto(CampaignNodeDto d) => CampaignModel(
        id: d.id, titulo: d.titulo, descripcion: d.descripcion,
        fechaInicio: d.fechaInicio, fechaFin: d.fechaFin, estado: d.estado,
        imagenUrl: d.imagenUrl, creadoPor: d.creadoPor,
        fechaCreacion: d.fechaCreacion ?? 0,
      );

  factory CampaignModel.fromFirestore(DocumentSnapshot doc) {
    final j = doc.data() as Map<String, dynamic>? ?? {};
    return CampaignModel(
      id: doc.id, titulo: parseStr(j['titulo']), descripcion: parseStr(j['descripcion']),
      fechaInicio: parseFechaDate(j['fechaInicio']), fechaFin: parseFechaDate(j['fechaFin']),
      estado: parseStr(j['estado']), imagenUrl: parseStr(j['imagenUrl']),
      creadoPor: parseStr(j['creadoPor']), fechaCreacion: parseInt(j['fechaCreacion']),
    );
  }

  Map<String, dynamic> toFirestoreMap() => {
        'id': id, 'titulo': titulo, 'descripcion': descripcion,
        'fechaInicio': fechaInicio, 'fechaFin': fechaFin, 'estado': estado,
        'imagenUrl': imagenUrl, 'creadoPor': creadoPor, 'fechaCreacion': fechaCreacion,
      };

  CampaignNodeDto toDto() => CampaignNodeDto(
        id: id, titulo: titulo, descripcion: descripcion, fechaInicio: fechaInicio,
        fechaFin: fechaFin, estado: estado, imagenUrl: imagenUrl, creadoPor: creadoPor,
        fechaCreacion: fechaCreacion,
      );
}

// ---------- HISTORIAL DE ESTADO ----------
class HistorialEstado {
  final String estado;
  final int fecha;
  final String cambiadoPor;
  const HistorialEstado({required this.estado, required this.fecha, required this.cambiadoPor});

  factory HistorialEstado.fromDto(HistorialNodeDto d) =>
      HistorialEstado(estado: d.estado, fecha: d.fecha ?? 0, cambiadoPor: d.cambiadoPor);

  factory HistorialEstado.fromMap(Map<String, dynamic> j) => HistorialEstado(
        estado: parseStr(j['estado']),
        fecha: parseInt(j['fecha'] ?? j['fechaCreacion']),
        cambiadoPor: parseStr(j['cambiadoPor']),
      );

  Map<String, dynamic> toMap() =>
      {'estado': estado, 'fecha': fecha, 'cambiadoPor': cambiadoPor};
}

// ---------- DONACIÓN ----------
class DonationModel {
  final String id;
  final String uid;
  final String campaignFirestoreId;
  final String campaignTitulo;
  final String nombre;
  final String descripcion;
  final String condicion;
  final String fotoUrl;
  final String estadoTrazabilidad;
  final String? motivoRechazo;
  final String materialTipo;
  final double co2AhorradoKg;
  final int fechaRegistro;
  final List<HistorialEstado> historial;

  const DonationModel({
    required this.id, required this.uid, required this.campaignFirestoreId,
    this.campaignTitulo = '', required this.nombre, this.descripcion = '',
    this.condicion = 'Buen Estado', this.fotoUrl = '',
    this.estadoTrazabilidad = 'Pendiente', this.motivoRechazo,
    this.materialTipo = '', this.co2AhorradoKg = 0, this.fechaRegistro = 0,
    this.historial = const [],
  });

  factory DonationModel.fromDto(DonationNodeDto d, {String campaignTitulo = ''}) =>
      DonationModel(
        id: d.id, uid: d.userId, campaignFirestoreId: d.campaignId,
        campaignTitulo: campaignTitulo, nombre: d.nombreJuguete,
        descripcion: d.descripcion, condicion: d.condicion, fotoUrl: d.fotoUrl,
        estadoTrazabilidad: d.estadoTrazabilidad, motivoRechazo: d.motivoRechazo,
        materialTipo: d.materialTipo, co2AhorradoKg: d.co2AhorradoKg,
        fechaRegistro: d.fechaRegistro ?? 0,
        historial: d.historial.map(HistorialEstado.fromDto).toList(),
      );

  factory DonationModel.fromFirestore(DocumentSnapshot doc) {
    final j = doc.data() as Map<String, dynamic>? ?? {};
    return DonationModel(
      id: doc.id,
      uid: parseStr(j['uid']),
      campaignFirestoreId: parseStr(j['campaignFirestoreId']),
      campaignTitulo: parseStr(j['campaignTitulo']),
      nombre: parseStr(j['nombre']),
      descripcion: parseStr(j['descripcion']),
      condicion: parseStr(j['estado']),
      fotoUrl: parseStr(j['fotoUrl']),
      estadoTrazabilidad: parseStr(j['estadoTrazabilidad']),
      motivoRechazo: j['motivoRechazo']?.toString(),
      materialTipo: parseStr(j['materialTipo']),
      co2AhorradoKg: parseDouble(j['co2AhorradoKg']),
      fechaRegistro: parseInt(j['fechaRegistro']),
      historial: (j['historialEstados'] as List<dynamic>? ?? [])
          .map((e) => HistorialEstado.fromMap(Map<String, dynamic>.from(e as Map)))
          .toList(),
    );
  }

  Map<String, dynamic> toFirestoreMap() => {
        'id': id, 'uid': uid, 'campaignFirestoreId': campaignFirestoreId,
        'campaignTitulo': campaignTitulo, 'nombre': nombre, 'descripcion': descripcion,
        'estado': condicion, 'fotoUrl': fotoUrl, 'estadoTrazabilidad': estadoTrazabilidad,
        'motivoRechazo': motivoRechazo, 'materialTipo': materialTipo,
        'co2AhorradoKg': co2AhorradoKg, 'fechaRegistro': fechaRegistro,
        'historialEstados': historial.map((h) => h.toMap()).toList(),
      };

  DonationNodeDto toDto() => DonationNodeDto(
        id: id, userId: uid, campaignId: campaignFirestoreId, nombreJuguete: nombre,
        descripcion: descripcion, condicion: condicion, fotoUrl: fotoUrl,
        estadoTrazabilidad: estadoTrazabilidad, motivoRechazo: motivoRechazo,
        materialTipo: materialTipo, co2AhorradoKg: co2AhorradoKg,
        fechaRegistro: fechaRegistro,
      );
}

// ---------- MATERIAL ----------
class MaterialModel {
  final int id;
  final String tipoMaterial;
  final double co2AhorradoKg;
  final String descripcionImpacto;
  final String imagenUrl;
  const MaterialModel({
    required this.id, required this.tipoMaterial, required this.co2AhorradoKg,
    this.descripcionImpacto = '', this.imagenUrl = '',
  });

  factory MaterialModel.fromDto(MaterialNodeDto d) => MaterialModel(
        id: d.id, tipoMaterial: d.tipoMaterial, co2AhorradoKg: d.co2AhorradoKg,
        descripcionImpacto: d.descripcionImpacto, imagenUrl: d.imagenUrl,
      );

  // (nuevo) Semilla local de respaldo (idéntica al schema de MySQL).
  static const List<MaterialModel> seed = [
    MaterialModel(id: 1, tipoMaterial: 'Plástico ABS', co2AhorradoKg: 2.5),
    MaterialModel(id: 2, tipoMaterial: 'Madera', co2AhorradoKg: 1.8),
    MaterialModel(id: 3, tipoMaterial: 'Metal', co2AhorradoKg: 3.2),
    MaterialModel(id: 4, tipoMaterial: 'Peluche / Textil', co2AhorradoKg: 1.5),
    MaterialModel(id: 5, tipoMaterial: 'Cartón / Papel', co2AhorradoKg: 0.9),
    MaterialModel(id: 6, tipoMaterial: 'Silicona / Goma', co2AhorradoKg: 2.1),
    MaterialModel(id: 7, tipoMaterial: 'Tela reciclada', co2AhorradoKg: 1.2),
  ];
}

// ---------- PUNTO DE ENTREGA ----------
class PuntoEntregaModel {
  final String id;
  final String nombre;
  final String direccion;
  final String horario;
  final List<String> dias;
  final String horaInicio;
  final String horaFin;
  final double latitud;
  final double longitud;
  final bool activo;
  final String creadoPor;
  const PuntoEntregaModel({
    required this.id, required this.nombre, required this.direccion,
    this.horario = '', this.dias = const [], this.horaInicio = '', this.horaFin = '',
    required this.latitud, required this.longitud, this.activo = true, this.creadoPor = '',
  });

  factory PuntoEntregaModel.fromDto(DeliveryPointNodeDto d) => PuntoEntregaModel(
        id: d.id, nombre: d.nombre, direccion: d.direccion, horario: d.horario,
        dias: parseList(d.diasAtencion), horaInicio: d.horaInicio, horaFin: d.horaFin,
        latitud: d.latitud, longitud: d.longitud, activo: d.activo, creadoPor: d.creadoPor,
      );

  factory PuntoEntregaModel.fromFirestore(DocumentSnapshot doc) {
    final j = doc.data() as Map<String, dynamic>? ?? {};
    return PuntoEntregaModel(
      id: doc.id, nombre: parseStr(j['nombre']), direccion: parseStr(j['direccion']),
      horario: parseStr(j['horario']), dias: parseList(j['dias']),
      horaInicio: parseStr(j['horaInicio']), horaFin: parseStr(j['horaFin']),
      latitud: parseDouble(j['latitud']), longitud: parseDouble(j['longitud']),
      activo: parseBool(j['activo']), creadoPor: parseStr(j['creadoPor']),
    );
  }
}

// ---------- EVENTO DE RECOJO ----------
class PickupEventModel {
  final String id;
  final String campaignId;
  final String nombre;
  final String lugar;
  final String direccion;
  final String fecha;
  final String hora;
  final double latitud;
  final double longitud;
  final bool activo;
  const PickupEventModel({
    required this.id, required this.campaignId, required this.nombre,
    this.lugar = '', this.direccion = '', required this.fecha, required this.hora,
    required this.latitud, required this.longitud, this.activo = true,
  });

  factory PickupEventModel.fromDto(PickupEventNodeDto d) => PickupEventModel(
        id: d.id, campaignId: d.campaignId, nombre: d.nombre, lugar: d.lugar,
        direccion: d.direccion, fecha: d.fecha, hora: d.hora,
        latitud: d.latitud, longitud: d.longitud, activo: d.activo,
      );

  factory PickupEventModel.fromFirestore(DocumentSnapshot doc) {
    final j = doc.data() as Map<String, dynamic>? ?? {};
    return PickupEventModel(
      id: doc.id, campaignId: parseStr(j['campaignId']), nombre: parseStr(j['nombre']),
      lugar: parseStr(j['lugar']), direccion: parseStr(j['direccion']),
      fecha: parseFechaDate(j['fecha']), hora: parseStr(j['hora']),
      latitud: parseDouble(j['latitud']), longitud: parseDouble(j['longitud']),
      activo: parseBool(j['activo']),
    );
  }
}

// ---------- EVIDENCIA DE GALERÍA ----------
class EvidenciaModel {
  final String id;
  final String campaignId;
  final String fotoUrl;
  final String descripcion;
  final String subidoPor;
  final int fechaSubida;
  const EvidenciaModel({
    required this.id, required this.campaignId, required this.fotoUrl,
    this.descripcion = '', this.subidoPor = '', this.fechaSubida = 0,
  });

  factory EvidenciaModel.fromDto(GalleryNodeDto d) => EvidenciaModel(
        id: d.id, campaignId: d.campaignId, fotoUrl: d.fotoUrl,
        descripcion: d.descripcion, subidoPor: d.subidoPor,
        fechaSubida: d.fechaSubida ?? 0,
      );

  factory EvidenciaModel.fromFirestore(DocumentSnapshot doc) {
    final j = doc.data() as Map<String, dynamic>? ?? {};
    return EvidenciaModel(
      id: doc.id, campaignId: parseStr(j['campaignId']), fotoUrl: parseStr(j['fotoUrl']),
      descripcion: parseStr(j['descripcion']), subidoPor: parseStr(j['subidoPor']),
      fechaSubida: parseInt(j['fechaSubida']),
    );
  }
}

// ---------- NOTIFICACIÓN ----------
class NotificacionModel {
  final String id;
  final String uidDestinatario;
  final String titulo;
  final String mensaje;
  final String donacionId;
  final String tipo;
  final bool leida;
  final int fechaCreacion;
  const NotificacionModel({
    required this.id, required this.uidDestinatario, required this.titulo,
    required this.mensaje, this.donacionId = '', this.tipo = 'general',
    this.leida = false, this.fechaCreacion = 0,
  });

  factory NotificacionModel.fromDto(NotificationNodeDto d) => NotificacionModel(
        id: d.id, uidDestinatario: d.userId, titulo: d.titulo, mensaje: d.mensaje,
        donacionId: d.donationId ?? '', tipo: d.tipo, leida: d.leida,
        fechaCreacion: d.fechaCreacion ?? 0,
      );

  factory NotificacionModel.fromFirestore(DocumentSnapshot doc) {
    final j = doc.data() as Map<String, dynamic>? ?? {};
    return NotificacionModel(
      id: doc.id, uidDestinatario: parseStr(j['uidDestinatario']),
      titulo: parseStr(j['titulo']), mensaje: parseStr(j['mensaje']),
      donacionId: parseStr(j['donacionId']), tipo: parseStr(j['tipo']),
      leida: parseBool(j['leida']), fechaCreacion: parseInt(j['fechaCreacion']),
    );
  }

  Map<String, dynamic> toFirestoreMap() => {
        'id': id, 'uidDestinatario': uidDestinatario, 'titulo': titulo,
        'mensaje': mensaje, 'donacionId': donacionId, 'tipo': tipo,
        'leida': leida, 'fechaCreacion': fechaCreacion,
      };
}
