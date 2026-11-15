// (modificado) Archivo: ReJuegaApp.kt
// (modificado) Descripción: Application personalizada de ReJuega.
// (modificado) Cambio: Se inicializan FirebaseAuth, Firestore y AuthRepository.
package pe.rejuega.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pe.rejuega.app.data.local.ReJuegaDatabase
import pe.rejuega.app.data.remote.AuthRepository
import pe.rejuega.app.data.remote.DonationRepository
import pe.rejuega.app.data.repository.CampaignRepository
import pe.rejuega.app.data.repository.JugueteRepository
import pe.rejuega.app.di.AppContainer
import pe.rejuega.app.data.repository.PickupEventRepository
import pe.rejuega.app.data.repository.PuntoEntregaRepository
import pe.rejuega.app.data.repository.CampaignFirestoreRepository
import pe.rejuega.app.data.repository.CampaignSyncRepository
import pe.rejuega.app.utils.DatosSemilla
import pe.rejuega.app.data.repository.DonationFirestoreRepository
import pe.rejuega.app.data.repository.NotificationFirestoreRepository
import pe.rejuega.app.data.repository.CampaignGalleryRepository
import pe.rejuega.app.data.repository.PuntoRecojoFirestoreRepository
import pe.rejuega.app.data.repository.GeocodingRepository
import pe.rejuega.app.data.repository.MaterialEcoRepository
import java.io.File
// (modificado) Clase Application.
class ReJuegaApp : Application() {

    // (modificado) Contenedor global de dependencias.
    lateinit var container: AppContainer

    // (modificado) Scope para inicialización asíncrona.
    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        // (nuevo) Configuración de osmdroid: User-Agent y caché de tiles (política OSM).
        org.osmdroid.config.Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = File(cacheDir, "osmdroid")
            osmdroidTileCache = File(File(cacheDir, "osmdroid"), "tiles")
        }

        // (nuevo) Inicializamos Firebase explícitamente.
        // (nuevo) Normalmente Firebase se inicializa solo, pero esto hace el proceso más claro.
        FirebaseApp.initializeApp(this)

        // (modificado) Inicializamos Room.
        val database = ReJuegaDatabase.getDatabase(this)

        // (modificado) Repositorios locales.
        val campaignRepository = CampaignRepository(database.campaignDao())
        val jugueteRepository = JugueteRepository(database.jugueteDao())

        // (nuevo) Instancias de Firebase.
        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseFirestore = FirebaseFirestore.getInstance()
        // (nuevo)
        val notificationFirestoreRepository = NotificationFirestoreRepository(firebaseFirestore)

        // (nuevo) Repositorio de autenticación.
        val authRepository = AuthRepository(
            auth = firebaseAuth,
            firestore = firebaseFirestore
        )

        // (nuevo) Repositorio de donaciones.
        val donationRepository = DonationRepository(
            firestore = firebaseFirestore,
            jugueteDao = database.jugueteDao()
        )


        // (modificado) Archivo: ReJuegaApp.kt - dentro de onCreate(), antes de crear container
        // (nuevo) Repositorio de puntos de entrega fijos.
        val puntoEntregaRepository = PuntoEntregaRepository(
            puntoEntregaDao = database.puntoEntregaDao()
        )

        // (nuevo) Repositorio de eventos de recojo por campaña.
        val pickupEventRepository = PickupEventRepository(
            pickupEventDao = database.pickupEventDao()
        )
        // (nuevo) Repository de campañas en Firestore.
        val campaignFirestoreRepository = CampaignFirestoreRepository(
            firestore = firebaseFirestore
        )

        // (nuevo) Repository de sincronización offline.
        val campaignSyncRepository = CampaignSyncRepository(
            campaignDao = database.campaignDao(),
            firestore = firebaseFirestore
        )

        // (nuevo) Dentro de onCreate(), antes de crear container:
        val donationFirestoreRepository = DonationFirestoreRepository(
            firestore = firebaseFirestore
        )


        // (nuevo) Repositorio de galería de evidencias.
        val campaignGalleryRepository = CampaignGalleryRepository(firebaseFirestore)

        // (nuevo) Repositorio de puntos de recojo y eventos en Firestore.
        val puntoRecojoFirestoreRepository = PuntoRecojoFirestoreRepository(firebaseFirestore)

        // (nuevo) Repositorio de geocodificación (Nominatim).
        val geocodingRepository = GeocodingRepository()

        // (nuevo) Repositorio de materiales ecológicos (MockAPI).
        val materialEcoRepository = MaterialEcoRepository()

        // (modificado) Guardamos todas las dependencias.
        // (modificado) Archivo: ReJuegaApp.kt - creación del container
        container = AppContainer(
            campaignRepository = campaignRepository,
            jugueteRepository = jugueteRepository,
            authRepository = authRepository,
            donationRepository = donationRepository,
            // (nuevo) Agregamos los repositorios de puntos de recojo.
            puntoEntregaRepository = puntoEntregaRepository,
            pickupEventRepository = pickupEventRepository,
            // (nuevo)
            campaignFirestoreRepository = campaignFirestoreRepository,
            // (nuevo)
            campaignSyncRepository = campaignSyncRepository,

            donationFirestoreRepository = donationFirestoreRepository,

            notificationFirestoreRepository = notificationFirestoreRepository,

            campaignGalleryRepository = campaignGalleryRepository,

            puntoRecojoFirestoreRepository = puntoRecojoFirestoreRepository,

            geocodingRepository = geocodingRepository,

            materialEcoRepository = materialEcoRepository
        )

        // (modificado) Insertamos datos semilla locales.
        inicializarDatosSemilla()
    }

    // (modificado) Carga datos hardcodeados locales.
    // (modificado) Archivo: ReJuegaApp.kt - función inicializarDatosSemilla
    // (modificado) Cambio: Se agregan las inserciones de puntos de entrega y eventos de recojo.
    private fun inicializarDatosSemilla() {
        applicationScope.launch {
            try {
                // (nuevo) Insertar campañas semilla en Firestore (solo si está vacío).
                container.campaignFirestoreRepository.inicializarCampaignsSemilla(
                    DatosSemilla.listaCampaigns
                )
                container.campaignRepository.inicializarDatosSemilla()
                container.jugueteRepository.inicializarDatosSemilla()
                // (nuevo) Insertamos puntos de entrega fijos de prueba.
                container.puntoEntregaRepository.inicializarDatosSemilla()
                // (nuevo) Insertamos eventos de recojo de prueba.
                container.pickupEventRepository.inicializarDatosSemilla()
            } catch (e: Exception) {
                println("Error inicializando datos semilla: ${e.message}")
            }
        }
    }
}