plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    // (nuevo) Aplicamos KSP en el módulo de la app
    // (nuevo) KSP para Room
    id("com.google.devtools.ksp")
    // Add the Google services Gradle plugin
    // (nuevo) Plugin de Firebase/Google Services
    id("com.google.gms.google-services")


}

android {
    namespace = "pe.rejuega.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        //  Debe coincidir con el paquete registrado en Firebase
        applicationId = "pe.rejuega.app"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // ==========================================================
    // (nuevo) DEPENDENCIAS PARA ARQUITECTURA MVVM Y ROOM
    // ==========================================================

    // (nuevo) Room: Base de datos local (SQLite abstraído)
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5") // (nuevo) Soporte para Corrutinas en Room
    ksp("androidx.room:room-compiler:2.8.5")       // (nuevo) Compilador de Room usando KSP

    // (nuevo) Lifecycle & ViewModel: Para manejar el estado de la UI sin bloquear la pantalla
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    // (nuevo) Navigation Compose: Para movernos entre pantallas (Login -> Home -> Detalle)
    implementation("androidx.navigation:navigation-compose:2.8.2")

    // (nuevo) Corrutinas: Para operaciones asíncronas (Red y BD) sin congelar la app
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // (nuevo) Coil: Librería para cargar imágenes desde URL en Jetpack Compose
    // (nuevo) La usaremos para mostrar las fotos de las campañas y juguetes
    implementation("io.coil-kt:coil-compose:2.5.0")

    // (nuevo) Paquete completo de íconos Material (History, DateRange, VerifiedUser, etc.)
    // (nuevo) Sin esta dependencia aparece: Unresolved reference: DateRange / History
    implementation("androidx.compose.material:material-icons-extended:1.7.4")


    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))


    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    // (nuevo) Firebase Authentication
    implementation("com.google.firebase:firebase-auth")
    // (nuevo) Cloud Firestore
    implementation("com.google.firebase:firebase-firestore")
    // (nuevo) Soporte de corrutinas para tareas de Firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")


    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries

}