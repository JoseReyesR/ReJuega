// (modificado) Archivo: AppContainer.kt
// (modificado) Cambio: Se agregan PuntoEntregaRepository y PickupEventRepository.
package pe.rejuega.app.di

import pe.rejuega.app.data.remote.AuthRepository
import pe.rejuega.app.data.remote.DonationRepository
import pe.rejuega.app.data.repository.CampaignRepository
import pe.rejuega.app.data.repository.JugueteRepository
import pe.rejuega.app.data.repository.PickupEventRepository
import pe.rejuega.app.data.repository.PuntoEntregaRepository
import pe.rejuega.app.data.repository.CampaignFirestoreRepository
import pe.rejuega.app.data.repository.CampaignSyncRepository
import pe.rejuega.app.data.repository.DonationFirestoreRepository
import pe.rejuega.app.data.repository.NotificationFirestoreRepository
import pe.rejuega.app.data.repository.CampaignGalleryRepository
// (modificado) Contenedor manual de dependencias.
class AppContainer(
    val campaignRepository: CampaignRepository,
    val jugueteRepository: JugueteRepository,
    val authRepository: AuthRepository,
    val donationRepository: DonationRepository,
    // (nuevo) Repositorio de puntos de entrega fijos (para el mapa futuro).
    val puntoEntregaRepository: PuntoEntregaRepository,
    // (nuevo) Repositorio de eventos de recojo por campaña (para el mapa futuro).
    val pickupEventRepository: PickupEventRepository,
    // (nuevo) Repository de campañas en Firestore.
    val campaignFirestoreRepository: CampaignFirestoreRepository,
    // (nuevo) Repository de sincronización offline.
    val campaignSyncRepository: CampaignSyncRepository,
    // (nuevo)
    val donationFirestoreRepository: DonationFirestoreRepository,
    val notificationFirestoreRepository: NotificationFirestoreRepository,
    // (nuevo) Repositorio de galería de evidencias.
    val campaignGalleryRepository: CampaignGalleryRepository
)