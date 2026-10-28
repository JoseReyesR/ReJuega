// (modificado) Archivo: AppContainer.kt
// (modificado) Cambio: Se agrega DonationRepository para el registro real de donaciones.
package pe.rejuega.app.di

import pe.rejuega.app.data.remote.AuthRepository
import pe.rejuega.app.data.remote.DonationRepository
import pe.rejuega.app.data.repository.CampaignRepository
import pe.rejuega.app.data.repository.JugueteRepository

// (modificado) Contenedor manual de dependencias.
class AppContainer(
    val campaignRepository: CampaignRepository,
    val jugueteRepository: JugueteRepository,
    val authRepository: AuthRepository,
    // (nuevo) Repositorio para registrar donaciones con foto en Cloudinary + Firestore.
    val donationRepository: DonationRepository
)