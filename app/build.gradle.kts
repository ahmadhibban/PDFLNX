plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.pdflnx.ahmad"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pdflnx.ahmad"
        minSdk = 23
        targetSdk = 28
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget("17"))
        }
    }
}

// Exclude OpenMP flavor to avoid duplicate native libraries
configurations.all {
    exclude(group = "cz.adaptech.tesseract4android", module = "tesseract4android-openmp")
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Tesseract 4 Android OCR Engine
    implementation("cz.adaptech:tesseract4android:4.3.0")
}
