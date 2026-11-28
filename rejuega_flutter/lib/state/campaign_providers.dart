// (nuevo) Providers que alimentan las pantallas del donante.
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/models/app_models.dart';
import '../data/repositories/repo_utils.dart';
import 'data_providers.dart';

// (nuevo) Campañas con indicador de origen (Node o respaldo).
final campaignsProvider = FutureProvider<Resultado<List<CampaignModel>>>(
  (ref) => ref.watch(campaignRepositoryProvider).obtenerConOrigen(),
);

// (nuevo) Detalle de campaña por id.
final campaignByIdProvider = FutureProvider.family<CampaignModel, String>(
  (ref, id) => ref.watch(campaignRepositoryProvider).obtenerPorId(id),
);

// (nuevo) Eventos de recojo de una campaña.
final eventsByCampaignProvider = FutureProvider.family<List<PickupEventModel>, String>(
  (ref, id) => ref.watch(puntoRecojoRepositoryProvider).obtenerEventosPorCampaign(id),
);

// (nuevo) Materiales (catálogo + dropdown del formulario).
final materialsProvider = FutureProvider<List<MaterialModel>>(
  (ref) => ref.watch(materialRepositoryProvider).obtenerMateriales(),
);

// (nuevo) Donaciones del donante (huella y trazabilidad).
final myDonationsProvider = FutureProvider.family<List<DonationModel>, String>(
  (ref, uid) => ref.watch(donationRepositoryProvider).obtenerPorUsuario(uid),
);

// (nuevo) Notificaciones del usuario y conteo de no leídas.
final myNotificationsProvider = FutureProvider.family<List<NotificacionModel>, String>(
  (ref, uid) => ref.watch(notificationRepositoryProvider).obtenerPorUsuario(uid),
);

final unreadCountProvider = FutureProvider.family<int, String>(
  (ref, uid) => ref.watch(notificationRepositoryProvider).contarNoLeidas(uid),
);
