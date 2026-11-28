// (nuevo) Equivalentes a NodeApiService.kt de Retrofit, pero con dio.
import '../dtos/node_dtos.dart';
import '../dtos/parse_utils.dart';
import 'http_client.dart';

class NodeApi {
  // ---------- MATERIALES ----------
  Future<List<MaterialNodeDto>> getMateriales() async {
    final r = await HttpClient.instance.get('/api/materiales');
    _check(r.statusCode);
    return ApiListResponse.fromJson(Map<String, dynamic>.from(r.data), MaterialNodeDto.fromJson).data;
  }

  // ---------- CAMPAÑAS ----------
  // (modificado) Con logs para ver la respuesta cruda del servidor.
  Future<List<CampaignNodeDto>> getCampaigns({String? estado}) async {
    print('>>> API: GET /api/campaigns ${estado != null ? "?estado=$estado" : ""}');

    final r = await HttpClient.instance.get(
      '/api/campaigns',
      queryParameters: estado == null ? null : {'estado': estado},
    );

    print('>>> API: Status ${r.statusCode}');
    print('>>> API: Response type: ${r.data.runtimeType}');

    if (r.statusCode == null || r.statusCode! < 200 || r.statusCode! >= 300) {
      throw Exception('HTTP ${r.statusCode}');
    }

    final responseData = r.data;
    print('>>> API: Response keys: ${responseData is Map ? responseData.keys.toList() : "NOT A MAP"}');

    if (responseData is Map) {
      print('>>> API: success=${responseData['success']}, count=${responseData['count']}');
      print('>>> API: data is ${responseData['data']?.runtimeType}, length=${(responseData['data'] as List?)?.length ?? 0}');
    }

    final result = ApiListResponse.fromJson(
      Map<String, dynamic>.from(responseData as Map),
      CampaignNodeDto.fromJson,
    );

    print('>>> API: Parsed ${result.data.length} campaigns');
    return result.data;
  }

  Future<CampaignNodeDto> getCampaign(String id) async {
    final r = await HttpClient.instance.get('/api/campaigns/$id');
    _check(r.statusCode);
    return CampaignNodeDto.fromJson(Map<String, dynamic>.from(r.data['data']));
  }

  Future<void> createCampaign(CampaignNodeDto dto) async {
    final r = await HttpClient.instance.post('/api/campaigns', data: dto.toJson());
    _check(r.statusCode);
  }

  Future<void> updateCampaign(String id, CampaignNodeDto dto) async {
    final r = await HttpClient.instance.put('/api/campaigns/$id', data: dto.toJson());
    _check(r.statusCode);
  }

  Future<void> updateCampaignEstado(String id, String estado) async {
    final r = await HttpClient.instance.put('/api/campaigns/$id/estado', data: {'estado': estado});
    _check(r.statusCode);
  }

  // ---------- DONACIONES ----------
  Future<List<DonationNodeDto>> getDonations({String? userId, String? campaignId}) async {
    final r = await HttpClient.instance.get('/api/donations', queryParameters: {
      if (userId != null) 'user_id': userId,
      if (campaignId != null) 'campaign_id': campaignId,
    });
    _check(r.statusCode);
    return ApiListResponse.fromJson(Map<String, dynamic>.from(r.data), DonationNodeDto.fromJson).data;
  }

  Future<void> createDonation(DonationNodeDto dto) async {
    final r = await HttpClient.instance.post('/api/donations', data: dto.toJson());
    _check(r.statusCode);
  }

  Future<void> updateDonation(String id, DonationNodeDto dto) async {
    final r = await HttpClient.instance.put('/api/donations/$id', data: dto.toJson());
    _check(r.statusCode);
  }

  Future<void> updateDonationEstado(String id, String estado, String cambiadoPor, {String? motivo}) async {
    final r = await HttpClient.instance.put('/api/donations/$id/estado', data: {
      'estado_trazabilidad': estado,
      'cambiado_por': cambiadoPor,
      if (motivo != null) 'motivo_rechazo': motivo,
    });
    _check(r.statusCode);
  }

  Future<void> deleteDonation(String id) async {
    final r = await HttpClient.instance.delete('/api/donations/$id');
    _check(r.statusCode);
  }

  // ---------- PUNTOS Y EVENTOS ----------
  Future<List<DeliveryPointNodeDto>> getDeliveryPoints() async {
    final r = await HttpClient.instance.get('/api/delivery-points');
    _check(r.statusCode);
    return ApiListResponse.fromJson(Map<String, dynamic>.from(r.data), DeliveryPointNodeDto.fromJson).data;
  }

  Future<List<PickupEventNodeDto>> getPickupEvents({String? campaignId}) async {
    final r = await HttpClient.instance.get(
      '/api/pickup-events',
      queryParameters: campaignId == null ? null : {'campaign_id': campaignId},
    );
    _check(r.statusCode);
    return ApiListResponse.fromJson(Map<String, dynamic>.from(r.data), PickupEventNodeDto.fromJson).data;
  }

  // ---------- GALERÍA ----------
  Future<List<GalleryNodeDto>> getGallery(String campaignId) async {
    final r = await HttpClient.instance.get('/api/campaign-gallery/campaign/$campaignId');
    _check(r.statusCode);
    return ApiListResponse.fromJson(Map<String, dynamic>.from(r.data), GalleryNodeDto.fromJson).data;
  }

  // ---------- NOTIFICACIONES ----------
  Future<List<NotificationNodeDto>> getNotifications(String uid) async {
    final r = await HttpClient.instance.get('/api/notifications/user/$uid');
    _check(r.statusCode);
    return ApiListResponse.fromJson(Map<String, dynamic>.from(r.data), NotificationNodeDto.fromJson).data;
  }

  Future<int> getUnreadCount(String uid) async {
    final r = await HttpClient.instance.get('/api/notifications/user/$uid/unread-count');
    _check(r.statusCode);
    final data = r.data['data'];
    if (data is Map) return parseInt(data['count']);
    return 0;
  }

  Future<void> markRead(String id) async {
    final r = await HttpClient.instance.put('/api/notifications/$id/read');
    _check(r.statusCode);
  }

  Future<void> markAllRead(String uid) async {
    final r = await HttpClient.instance.put('/api/notifications/user/$uid/read-all');
    _check(r.statusCode);
  }

  Future<void> createNotification(NotificationNodeDto dto) async {
    final r = await HttpClient.instance.post('/api/notifications', data: {
      'id': dto.id, 'user_id': dto.userId, 'titulo': dto.titulo,
      'mensaje': dto.mensaje, 'donation_id': dto.donationId, 'tipo': dto.tipo,
    });
    _check(r.statusCode);
  }

  // (nuevo) Lanza excepción si el status no es 2xx → activa el respaldo.
  void _check(int? status) {
    if (status == null || status < 200 || status >= 300) {
      throw Exception('HTTP $status');
    }
  }
}
