// (modificado) Archivo: AppContainer.kt
// (modificado) Descripción: Contenedor simple de dependencias.
// (modificado) Cambio: Se agrega AuthRepository para Firebase Authentication.
package pe.rejuega.app.di

import pe.rejuega.app.data.remote.AuthRepository
import pe.rejuega.app.data.repository.CampaignRepository
import pe.rejuega.app.data.repository.JugueteRepository

// (modificado) Contenedor manual de dependencias.
class AppContainer(
    // (modificado) Repositorio de campañas locales.
    val campaignRepository: CampaignRepository,

    // (modificado) Repositorio de juguetes locales.
    val jugueteRepository: JugueteRepository,

    // (nuevo) Repositorio de autenticación con Firebase.
    val authRepository: AuthRepository
)