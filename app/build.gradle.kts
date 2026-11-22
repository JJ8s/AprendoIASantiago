import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Guía 3.3.1: las claves de firma viven en keystore.properties (fuera de Git), nunca en este archivo.
val archivoClaves = rootProject.file("keystore.properties")
val claves = Properties().apply { if (archivoClaves.exists()) archivoClaves.inputStream().use { load(it) } }

android {
    namespace = "com.example.aprendoiasantiago"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.aprendoiasantiago"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Direcciones de los microservicios Spring Boot. 10.0.2.2 es el computador visto desde el emulador.
        // En un teléfono real: ./gradlew assembleDebug -PurlUsuarios=http://192.168.1.50:8081/api/ -PurlContenido=http://192.168.1.50:8082/api/
        val urlUsuarios = (project.findProperty("urlUsuarios") as String?) ?: "http://10.0.2.2:8081/api/"
        val urlContenido = (project.findProperty("urlContenido") as String?) ?: "http://10.0.2.2:8082/api/"
        buildConfigField("String", "URL_USUARIOS", "\"$urlUsuarios\"")
        buildConfigField("String", "URL_CONTENIDO", "\"$urlContenido\"")
    }

    signingConfigs {
        // Solo existe si el archivo keystore.properties está presente: sin él, el proyecto sigue sincronizando.
        if (archivoClaves.exists()) {
            create("release") {
                storeFile = rootProject.file(claves.getProperty("storeFile"))
                storePassword = claves.getProperty("storePassword")
                keyAlias = claves.getProperty("keyAlias")
                keyPassword = claves.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            signingConfigs.findByName("release")?.let { signingConfig = it }
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
        buildConfig = true
    }
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Android / Lifecycle / Navegación
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Consumo de API REST (Retrofit + Gson)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Persistencia local (Room + KSP)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Mapa interactivo gratuito (OpenStreetMap, sin API key)
    implementation(libs.osmdroid.android)

    // Escáner QR: CameraX + ML Kit (lectura en el propio móvil, sin internet)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.barcode.scanning)
    // Validación de la selfie: detecta el rostro en el propio móvil
    implementation(libs.mlkit.face.detection)

    // Reproducción de las cápsulas de video
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    // Tests unitarios (src/test) — Guía 15: testing híbrido
    testImplementation(libs.junit)                         // JUnit 4: tests anteriores del proyecto
    testRuntimeOnly(libs.junit.vintage.engine)             // permite que JUnit 4 siga corriendo con JUnit 5
    testImplementation(libs.junit.jupiter)                 // JUnit 5
    testImplementation(libs.kotest.runner.junit5)          // Kotest: ejecuta las specs sobre JUnit 5
    testImplementation(libs.kotest.assertions.core)        // Kotest: shouldBe, shouldContainExactly...
    testImplementation(libs.mockk)                         // MockK: simula DAOs, repositorios y ViewModels
    testImplementation(libs.kotlinx.coroutines.test)       // runTest y dispatchers de prueba

    // Tests de interfaz (src/androidTest): Compose UI Test + JUnit 4
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// Obligatorio para usar JUnit 5 (y Kotest) en los tests unitarios.
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
