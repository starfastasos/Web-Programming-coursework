plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt") // Annotation Processor for Kotlin (e.g., Room)
}

android {
    namespace = "com.example.mysupermarketapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mysupermarketapplication"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Set to true for production builds to enable code shrinking
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        compose = true // Enables Jetpack Compose features
    }
}

dependencies {
    // Define versions for consistency
    val room_version = "2.6.1"
    val nav_version = "2.7.7"
    val coroutines_version = "1.8.0"
    val lifecycle_version = "2.8.0-beta01"

    // Jetpack Compose UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.foundation:foundation:1.6.7")
    implementation("androidx.compose.material:material-icons-extended")

    // Room Database
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    // Jetpack Navigation for Compose
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // Kotlin Coroutines for asynchronous operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")

    // Jetpack Lifecycle components
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")

    // Coil for image loading in Compose
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Core Android KTX and Activity Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Unit and Instrumentation Tests
    testImplementation(libs.junit) // Local unit tests
    androidTestImplementation(libs.androidx.junit) // Android instrumentation tests
    androidTestImplementation(libs.androidx.espresso.core) // UI testing framework
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4) // Compose UI testing

    // Debugging tools for Compose
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Google GSON for JSON serialization/deserialization
    implementation("com.google.code.gson:gson:2.10.1")
}
