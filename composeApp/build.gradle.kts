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
            jvmTarget.set(JvmTarget.JVM_17) // Changed to JVM 17
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17) // Set JVM 17 for desktop target too
        }
    }

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
            implementation(libs.okio) // Add okio for Android
            implementation(libs.androidx.work.runtime.ktx) // Add WorkManager
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            // implementation(compose.material) // Remove Material 2 dependency
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
            implementation(libs.filechooser) // Add file chooser dependency

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

            // File operations
            implementation(libs.okio) // Add okio for file operations
            implementation(libs.csvReader) // Add CSV library

            implementation(libs.kotlinx.datetime)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp) // Ensure Ktor OkHttp is here
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.androidx.material3.desktop)
            implementation(libs.okio) // Add okio for Desktop
            implementation(libs.datetime.wheel.picker)
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
        sourceCompatibility = JavaVersion.VERSION_17 // Changed to 17
        targetCompatibility = JavaVersion.VERSION_17 // Changed to 17
    }
}
dependencies {
    implementation(libs.androidx.material3.android)
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
            // Explicitly include the java modules needed for the application
            modules(
                "java.compiler",
                "java.instrument",
                "java.sql",
                "jdk.unsupported",
                "java.logging",
                "java.desktop", // Add java.desktop module
                "jdk.jsobject" // Add jsobject module
            )

            windows {
                includeAllModules = true
                // Ensure menu icons are visible
                menuGroup = "WordBook"
            }
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
