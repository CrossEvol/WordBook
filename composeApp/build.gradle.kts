import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization")
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.sqldelight.android.driver)
            // Moved Android-specific dependencies here from the removed block
            implementation(libs.androidx.material3.android)
            implementation(libs.androidx.foundation.layout.android)
            implementation(libs.androidx.ui.android)
            implementation(libs.androidx.ui.tooling.preview.android)
            implementation(compose.uiTooling)
            implementation(libs.ktor.client.android) // Ensure Ktor Android is here
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material) // This is Compose Material 2, keep if needed, otherwise remove
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // ViewModel dependencies
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose) // Added dependency for getViewModel

            // Common Material 3 dependency (correct for common code)
            implementation(libs.androidx.material3)
            // Removed: implementation(libs.androidx.material3.android) // This was the problem!

            // Kotlinx Serialization
            implementation(libs.kotlinx.serialization.json)

            // Ktor common
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            // SQLDelight common
            implementation(libs.sqldelight.runtime)

            // Multiplatform Settings
            implementation(libs.multiplatform.settings)

            // Kotlin Logging (SLF4J API)
            implementation(libs.kotlin.logging)
            // SLF4J Simple Provider for common logging output (Console/Logcat)
            implementation(libs.slf4j.simple) // Added slf4j-simple provider

            // Removed: implementation(libs.compose.hotpreview.jvm) // Moved to desktopMain
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp) // Ensure Ktor OkHttp is here
            implementation(libs.sqldelight.sqlite.driver)

        }
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.crossevol.wordbook.db")
        }
    }
}

android {
    namespace = "com.crossevol.wordbook"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.crossevol.wordbook"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Removed the redundant dependencies block here
/*
dependencies {
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.ui.tooling.preview.android)
    debugImplementation(compose.uiTooling)
    // https://mvnrepository.com/artifact/de.drick.compose/hotpreview
    implementation(libs.hotpreview)
}
*/


compose.desktop {
    application {
        mainClass = "com.crossevol.wordbook.MainKt"

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Deb
            )
            packageName = "com.crossevol.wordbook"
            packageVersion = "1.0.0"

        }
    }
}
