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

// (modificado) Clase Application.
class ReJuegaApp : Application() {

    // (modificado) Contenedor global de dependencias.
    lateinit var container: AppContainer

    // (modificado) Scope para inicialización asíncrona.
    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()

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

        // (modificado) Guardamos todas las dependencias.
        container = AppContainer(
            campaignRepository = campaignRepository,
            jugueteRepository = jugueteRepository,
            authRepository = authRepository,
            // (nuevo) Agregamos el nuevo repositorio de donaciones.
            donationRepository = donationRepository
        )

        // (modificado) Insertamos datos semilla locales.
        inicializarDatosSemilla()
    }

    // (modificado) Carga datos hardcodeados locales.
    private fun inicializarDatosSemilla() {
        applicationScope.launch {
            try {
                container.campaignRepository.inicializarDatosSemilla()
                container.jugueteRepository.inicializarDatosSemilla()
            } catch (e: Exception) {
                println("Error inicializando datos semilla: ${e.message}")
            }
        }
    }
}