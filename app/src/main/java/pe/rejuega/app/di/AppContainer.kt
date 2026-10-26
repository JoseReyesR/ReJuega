// (nuevo) Archivo: AppContainer.kt
// (nuevo) Descripción: Contenedor simple de dependencias (Inyección de Dependencias manual).
// (nuevo) Motivo: Evitamos usar Hilt/Dagger por ahora para mantener el proyecto simple y académico.
// (nuevo) En este contenedor guardamos los Repositories que usarán los ViewModels.
package pe.rejuega.app.di

import pe.rejuega.app.data.repository.CampaignRepository

// (nuevo) Clase que agrupa todas las dependencias que la app necesita.
// (nuevo) Se instancia una sola vez en la clase Application (ReJuegaApp).
class AppContainer(
    // (nuevo) Repository de campañas: será usado por el ViewModel de la pantalla Home.
    val campaignRepository: CampaignRepository
    // (nuevo) Aquí agregaremos más repositories en futuros pasos:
    // val jugueteRepository: JugueteRepository,
    // val puntoEntregaRepository: PuntoEntregaRepository,
    // val materialEcoRepository: MaterialEcoRepository
)