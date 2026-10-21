plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.conysolis.georegistro"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.conysolis.georegistro"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["mapsApiKey"] = project.findProperty("MAPS_API_KEY") ?: ""
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    // AndroidX Core y UI
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // Firebase Cloud Firestore
    implementation("com.google.firebase:firebase-firestore:24.10.0")

    // Servicios de Ubicación (GPS)
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // OpenStreetMap (Mapa interactivo sin API Keys)
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // Pruebas Unitarias e Instrumentadas
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}