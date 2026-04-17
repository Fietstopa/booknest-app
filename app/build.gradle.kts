import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.gms.google-services")
}
val properties = Properties()
properties.load(project.rootProject.file("local.properties").reader())

val server = properties.getProperty("server")
android {
    namespace = "git.pef.mendelu.cz.booknest"
    compileSdk = 36

    defaultConfig {
        applicationId = "git.pef.mendelu.cz.booknest"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "git.pef.mendelu.cz.booknest.HiltTestRunner"
    }

    buildTypes {
        release {
            setFlavorDimensions(listOf("version"))
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            buildConfigField(type = "String", name = "SERVER_URL", value = server)
        }


    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.navigation.compose)
    implementation(libs.kotlin.serialization.json)

    // Datastore
    implementation(libs.datastore.core)
    implementation(libs.datastore.preferences)
    // Room
    implementation(libs.room.ktx)
    implementation(libs.room.viewmodel)
    implementation(libs.room.lifecycle)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler.ksp)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.compose)
    ksp(libs.hilt.compiler.ksp)

    // Moshi
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.ksp)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.retrofit.okhtt3)

    // coil
    implementation(libs.coil)



    // Splashscreen
    implementation(libs.splashscreen)
    // Maps
    implementation(libs.google.maps.compose)
    implementation(libs.google.maps.compose.utils)
    implementation(libs.google.play.services.maps)
    implementation(libs.google.play.services.auth)
    implementation(libs.google.play.services.location)
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("com.google.mlkit:text-recognition:16.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Testing hilt
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)
    testImplementation(libs.kotlinx.coroutines.test)
}
