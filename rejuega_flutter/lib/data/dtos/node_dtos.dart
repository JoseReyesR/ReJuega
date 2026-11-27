// (nuevo) Equivalentes a NodeDtos.kt de Kotlin: un solo archivo con todos los DTOs.
import 'parse_utils.dart';

// ---------- WRAPPERS GENÉRICOS DE RESPUESTA ----------

class ApiListResponse<T> {
  final bool success;
  final int count;
  final List<T> data;
  ApiListResponse({required this.success, required this.count, required this.data});

  factory ApiListResponse.fromJson(
    Map<String, dynamic> json,
    T Function(Map<String, dynamic>) from,
  ) {
    final list = (json['data'] as List<dynamic>? ?? [])
        .map((e) => from(Map<String, dynamic>.from(e as Map)))
        .toList();
    return ApiListResponse(
      success: parseBool(json['success']),
      count: parseInt(json['count']),
      data: list,
    );
  }
}

class ApiCreateResponse {
  final bool success;
  final String message;
  ApiCreateResponse({required this.success, required this.message});
  factory ApiCreateResponse.fromJson(Map<String, dynamic> json) => ApiCreateResponse(
        success: parseBool(json['success']),
        message: parseStr(json['message'] ?? json['error']),
      );
}

// ---------- MATERIALES ----------

class MaterialNodeDto {
  final int id;
  final String tipoMaterial;
  final double co2AhorradoKg;
  final String descripcionImpacto;
  final String imagenUrl;
  MaterialNodeDto({
    required this.id,
    required this.tipoMaterial,
    required this.co2AhorradoKg,
    required this.descripcionImpacto,
    required this.imagenUrl,
  });
  factory MaterialNodeDto.fromJson(Map<String, dynamic> j) => MaterialNodeDto(
        id: parseInt(j['id']),
        tipoMaterial: parseStr(j['tipo_material']),
        co2AhorradoKg: parseDouble(j['co2_ahorrado_kg']),
        descripcionImpacto: parseStr(j['descripcion_impacto']),
        imagenUrl: parseStr(j['imagen_url']),
      );
}

// ---------- CAMPAÑAS ----------

class CampaignNodeDto {
  final String id;
  final String titulo;
  final String descripcion;
  final String fechaInicio;
  final String fechaFin;
  final String estado;
  final String imagenUrl;
  final String creadoPor;
  final int? fechaCreacion;
  CampaignNodeDto({
    required this.id,
    required this.titulo,
    required this.descripcion,
    required this.fechaInicio,
    required this.fechaFin,
    required this.estado,
    required this.imagenUrl,
    required this.creadoPor,
    this.fechaCreacion,
  });
  factory CampaignNodeDto.fromJson(Map<String, dynamic> j) => CampaignNodeDto(
        id: parseStr(j['id']),
        titulo: parseStr(j['titulo']),
        descripcion: parseStr(j['descripcion']),
        fechaInicio: parseFechaDate(j['fecha_inicio']),
        fechaFin: parseFechaDate(j['fecha_fin']),
        estado: parseStr(j['estado']),
        imagenUrl: parseStr(j['imagen_url']),
        creadoPor: parseStr(j['creado_por']),
        fechaCreacion: parseMillis(j['fecha_creacion']),
      );

  // (nuevo) Para POST/PUT hacia Node.
  Map<String, dynamic> toJson() => {
        'id': id,
        'titulo': titulo,
        'descripcion': descripcion,
        'fecha_inicio': fechaInicio,
        'fecha_fin': fechaFin,
        'estado': estado,
        'imagen_url': imagenUrl,
        'creado_por': creadoPor,
      };
}

// ---------- DONACIONES ----------

class HistorialNodeDto {
  final String estado;
  final int? fecha;
  final String cambiadoPor;
  HistorialNodeDto({required this.estado, this.fecha, required this.cambiadoPor});
  factory HistorialNodeDto.fromJson(Map<String, dynamic> j) => HistorialNodeDto(
        estado: parseStr(j['estado']),
        fecha: parseMillis(j['fecha_cambio'] ?? j['fecha']),
        cambiadoPor: parseStr(j['cambiado_por'] ?? j['cambiadoPor']),
      );
}

class DonationNodeDto {
  final String id;
  final String userId;
  final String campaignId;
  final String nombreJuguete;
  final String descripcion;
  final String condicion;
  final String fotoUrl;
  final String estadoTrazabilidad;
  final String? motivoRechazo;
  final String materialTipo;
  final double co2AhorradoKg;
  final int? fechaRegistro;
  final List<HistorialNodeDto> historial;
  DonationNodeDto({
    required this.id,
    required this.userId,
    required this.campaignId,
    required this.nombreJuguete,
    required this.descripcion,
    required this.condicion,
    required this.fotoUrl,
    required this.estadoTrazabilidad,
    this.motivoRechazo,
    required this.materialTipo,
    required this.co2AhorradoKg,
    this.fechaRegistro,
    this.historial = const [],
  });
  factory DonationNodeDto.fromJson(Map<String, dynamic> j) => DonationNodeDto(
        id: parseStr(j['id']),
        userId: parseStr(j['user_id']),
        campaignId: parseStr(j['campaign_id']),
        nombreJuguete: parseStr(j['nombre_juguete']),
        descripcion: parseStr(j['descripcion']),
        condicion: parseStr(j['condicion']),
        fotoUrl: parseStr(j['foto_url']),
        estadoTrazabilidad: parseStr(j['estado_trazabilidad']),
        motivoRechazo: j['motivo_rechazo']?.toString(),
        materialTipo: parseStr(j['material_tipo']),
        co2AhorradoKg: parseDouble(j['co2_ahorrado_kg']),
        fechaRegistro: parseMillis(j['fecha_registro']),
        historial: (j['historial'] as List<dynamic>? ?? [])
            .map((e) => HistorialNodeDto.fromJson(Map<String, dynamic>.from(e as Map)))
            .toList(),
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'user_id': userId,
        'campaign_id': campaignId,
        'nombre_juguete': nombreJuguete,
        'descripcion': descripcion,
        'condicion': condicion,
        'foto_url': fotoUrl,
        'estado_trazabilidad': estadoTrazabilidad,
        'material_tipo': materialTipo,
        'co2_ahorrado_kg': co2AhorradoKg,
        'fecha_registro': fechaRegistro,
      };
}

// ---------- PUNTOS Y EVENTOS ----------

class DeliveryPointNodeDto {
  final String id;
  final String nombre;
  final String direccion;
  final String horario;
  final String diasAtencion;
  final String horaInicio;
  final String horaFin;
  final double latitud;
  final double longitud;
  final bool activo;
  final String creadoPor;
  DeliveryPointNodeDto({
    required this.id, required this.nombre, required this.direccion,
    required this.horario, required this.diasAtencion, required this.horaInicio,
    required this.horaFin, required this.latitud, required this.longitud,
    required this.activo, required this.creadoPor,
  });
  factory DeliveryPointNodeDto.fromJson(Map<String, dynamic> j) => DeliveryPointNodeDto(
        id: parseStr(j['id']), nombre: parseStr(j['nombre']),
        direccion: parseStr(j['direccion']), horario: parseStr(j['horario']),
        diasAtencion: parseStr(j['dias_atencion']), horaInicio: parseStr(j['hora_inicio']),
        horaFin: parseStr(j['hora_fin']), latitud: parseDouble(j['latitud']),
        longitud: parseDouble(j['longitud']), activo: parseBool(j['activo']),
        creadoPor: parseStr(j['creado_por']),
      );
}

class PickupEventNodeDto {
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
  final String creadoPor;
  PickupEventNodeDto({
    required this.id, required this.campaignId, required this.nombre,
    required this.lugar, required this.direccion, required this.fecha,
    required this.hora, required this.latitud, required this.longitud,
    required this.activo, required this.creadoPor,
  });
  factory PickupEventNodeDto.fromJson(Map<String, dynamic> j) => PickupEventNodeDto(
        id: parseStr(j['id']), campaignId: parseStr(j['campaign_id']),
        nombre: parseStr(j['nombre']), lugar: parseStr(j['lugar']),
        direccion: parseStr(j['direccion']), fecha: parseFechaDate(j['fecha']),
        hora: parseStr(j['hora']), latitud: parseDouble(j['latitud']),
        longitud: parseDouble(j['longitud']), activo: parseBool(j['activo']),
        creadoPor: parseStr(j['creado_por']),
      );
}

// ---------- GALERÍA ----------

class GalleryNodeDto {
  final String id;
  final String campaignId;
  final String fotoUrl;
  final String descripcion;
  final String subidoPor;
  final int? fechaSubida;
  GalleryNodeDto({
    required this.id, required this.campaignId, required this.fotoUrl,
    required this.descripcion, required this.subidoPor, this.fechaSubida,
  });
  factory GalleryNodeDto.fromJson(Map<String, dynamic> j) => GalleryNodeDto(
        id: parseStr(j['id']), campaignId: parseStr(j['campaign_id']),
        fotoUrl: parseStr(j['foto_url']), descripcion: parseStr(j['descripcion']),
        subidoPor: parseStr(j['subido_por']), fechaSubida: parseMillis(j['fecha_subida']),
      );
}

// ---------- NOTIFICACIONES ----------

class NotificationNodeDto {
  final String id;
  final String userId;
  final String titulo;
  final String mensaje;
  final String? donationId;
  final String tipo;
  final bool leida;
  final int? fechaCreacion;
  NotificationNodeDto({
    required this.id, required this.userId, required this.titulo,
    required this.mensaje, this.donationId, required this.tipo,
    required this.leida, this.fechaCreacion,
  });
  factory NotificationNodeDto.fromJson(Map<String, dynamic> j) => NotificationNodeDto(
        id: parseStr(j['id']), userId: parseStr(j['user_id']),
        titulo: parseStr(j['titulo']), mensaje: parseStr(j['mensaje']),
        donationId: j['donation_id']?.toString(), tipo: parseStr(j['tipo']),
        leida: parseBool(j['leida']), fechaCreacion: parseMillis(j['fecha_creacion']),
      );
}
