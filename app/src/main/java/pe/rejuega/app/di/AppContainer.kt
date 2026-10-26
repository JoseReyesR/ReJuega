// (modificado) Archivo: AppContainer.kt
// (modificado) Descripción: Contenedor simple de dependencias.
// (modificado) Cambio: Se agrega JugueteRepository.
package pe.rejuega.app.di

import pe.rejuega.app.data.repository.CampaignRepository
import pe.rejuega.app.data.repository.JugueteRepository

// (modificado) Contenedor manual de dependencias para mantener el proyecto simple.
class AppContainer(
    // (modificado) Repositorio de campañas.
    val campaignRepository: CampaignRepository,

    // (nuevo) Repositorio de juguetes.
    val jugueteRepository: JugueteRepository
)