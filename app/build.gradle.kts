plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    // (nuevo) Aplicamos KSP en el módulo de la app
    id("com.google.devtools.ksp")
}

android {
    namespace = "pe.rejuega.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
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
}