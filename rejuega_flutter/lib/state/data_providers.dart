// (nuevo) Providers únicos de cada repositorio (equivalente al AppContainer de Kotlin).
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/repositories/campaign_repository.dart';
import '../data/repositories/donation_repository.dart';
import '../data/repositories/gallery_repository.dart';
import '../data/repositories/material_repository.dart';
import '../data/repositories/notification_repository.dart';
import '../data/repositories/punto_recojo_repository.dart';
import '../data/services/node_api.dart';

final nodeApiProvider = Provider<NodeApi>((ref) => NodeApi());
final campaignRepositoryProvider = Provider<CampaignRepository>((ref) => CampaignRepository());
final donationRepositoryProvider = Provider<DonationRepository>((ref) => DonationRepository());
final materialRepositoryProvider = Provider<MaterialRepository>((ref) => MaterialRepository());
final notificationRepositoryProvider = Provider<NotificationRepository>((ref) => NotificationRepository());
final puntoRecojoRepositoryProvider = Provider<PuntoRecojoRepository>((ref) => PuntoRecojoRepository());
final galleryRepositoryProvider = Provider<GalleryRepository>((ref) => GalleryRepository());
