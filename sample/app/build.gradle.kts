plugins {
    id("com.android.application")
    kotlin("android")

    // Enable either v1 or v2
//    id("variant-v1-basis")
//    id("variant-v1-advanced")
    id("variant-v2-basis")
    id("variant-v2-advanced")
    id("variant-v2-polyfill")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "me.xx2bab.sample.ea"
        minSdk = 28
        targetSdk = 3
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }

    flavorDimensions += "server"
    productFlavors {
        create("staging") {
            dimension = "server"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
        }
        create("production") {
            dimension = "server"
            applicationIdSuffix = ".production"
            versionNameSuffix = "-production"
            versionCode = 2
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.2")
    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}

//androidComponents {
//    onVariants { variant ->
//        if (variant.name.contains("release", true)) {
//            variant.androidResources.aaptAdditionalParameters.add("-v")
//        } else {
//            variant.androidResources.aaptAdditionalParameters.add("-p")
//        }
//    }
//}