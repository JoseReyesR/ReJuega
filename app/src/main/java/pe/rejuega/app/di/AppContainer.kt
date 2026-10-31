// (modificado) Archivo: AppContainer.kt
// (modificado) Cambio: Se agregan PuntoEntregaRepository y PickupEventRepository.
package pe.rejuega.app.di

import pe.rejuega.app.data.remote.AuthRepository
import pe.rejuega.app.data.remote.DonationRepository
import pe.rejuega.app.data.repository.CampaignRepository
import pe.rejuega.app.data.repository.JugueteRepository
import pe.rejuega.app.data.repository.PickupEventRepository
import pe.rejuega.app.data.repository.PuntoEntregaRepository

// (modificado) Contenedor manual de dependencias.
class AppContainer(
    val campaignRepository: CampaignRepository,
    val jugueteRepository: JugueteRepository,
    val authRepository: AuthRepository,
    val donationRepository: DonationRepository,
    // (nuevo) Repositorio de puntos de entrega fijos (para el mapa futuro).
    val puntoEntregaRepository: PuntoEntregaRepository,
    // (nuevo) Repositorio de eventos de recojo por campaña (para el mapa futuro).
    val pickupEventRepository: PickupEventRepository
)