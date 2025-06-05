plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose)
    kotlin("plugin.serialization") version "2.1.20"
}

android {
    signingConfigs {
        create("defaultSignature") {
            storeFile = file(project.findProperty("StoreFile") ?: "droidlock.jks")
            storePassword = (project.findProperty("StorePassword") as String?) ?: "123456"
            keyPassword = (project.findProperty("KeyPassword") as String?) ?: "123456"
            keyAlias = (project.findProperty("KeyAlias") as String?) ?: "zzh"
        }
    }
    namespace = "com.zzh.droidlock"
    compileSdk = 35

    lint.checkReleaseBuilds = false
    lint.disable += "All"

    defaultConfig {
        applicationId = "com.zzh.droidlock"
        minSdk = 23
        targetSdk = 35
        versionCode = 38
        versionName = "1.0"
        multiDexEnabled = false
    }

    buildTypes {
        release {
//            isMinifyEnabled = true
//            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("defaultSignature")
            composeCompiler {
                includeSourceInformation = false
                includeTraceMarkers = false
            }
        }
        debug {
            signingConfig = signingConfigs.getByName("defaultSignature")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        aidl = true
    }
    androidResources {
        generateLocaleConfig = true
    }
    dependenciesInfo {
        includeInApk = false
    }
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }
    }
}

gradle.taskGraph.whenReady {
    project.tasks.findByPath(":app:test")?.enabled = false
    project.tasks.findByPath(":app:lint")?.enabled = false
    project.tasks.findByPath(":app:lintAnalyzeDebug")?.enabled = false
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.accompanist.drawablepainter)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.shizuku.provider)
    implementation(libs.shizuku.api)
    implementation(libs.dhizuku.api)
    implementation(libs.androidx.fragment)
    implementation(libs.hiddenApiBypass)
    implementation(libs.serialization)
    implementation(kotlin("reflect"))
}