import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

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
            implementation(libs.androidx.lifecycle.viewmodel.compose) // Added dependency for getViewModel

            // Common Material 3 dependency (correct for common code)
            implementation(libs.androidx.material3)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // ViewModel dependencies
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

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

    // --- Added Signing Configuration ---
    signingConfigs {
        create("release") {
            // It's highly recommended to load sensitive information like passwords
            // from environment variables or a separate properties file, NOT hardcoded.
            // Example using a properties file (create keystore.properties in project root):
            // storeFile=path/to/your/my-release-key.keystore
            // storePassword=your_keystore_password
            // keyAlias=my-alias
            // keyPassword=your_key_password

            val properties = Properties().apply {
                val keystorePropertiesFile = rootProject.file("key.properties")
                if (keystorePropertiesFile.exists()) {
                    load(FileInputStream(keystorePropertiesFile))
                } else {
                    // Provide default values or throw an error if the file is missing
                    // For now, we'll just log a warning or use empty strings
                    println("WARNING: keystore.properties not found. Release signing config may be incomplete.")
                }
            }

            storeFile = file(properties.getProperty("storeFile", "")) // Provide a default empty string if property is missing
            storePassword = properties.getProperty("storePassword", "")
            keyAlias = properties.getProperty("keyAlias", "")
            keyPassword = properties.getProperty("keyPassword", "")

            // Alternatively, use environment variables:
            // storeFile = file(System.getenv("KEYSTORE_PATH") ?: "")
            // storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            // keyAlias = System.getenv("KEY_ALIAS") ?: ""
            // keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }
    // --- End Signing Configuration ---

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false // Set to true to enable code shrinking and obfuscation
            // --- Apply Release Signing Config ---
            signingConfig = signingConfigs.getByName("release")
            // --- End Apply Release Signing Config ---
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // Changed to 17
        targetCompatibility = JavaVersion.VERSION_17 // Changed to 17
    }
}


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
                includeAllModules = false
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
