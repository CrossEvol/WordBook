package com.crossevol.wordbook

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.crossevol.wordbook.data.api.WordFetchApi
import com.crossevol.wordbook.db.DriverFactory
import com.russhwolf.settings.PreferencesSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.net.URI
import java.util.prefs.Preferences

fun main() = application {
    // Create the database driver factory for desktop
    val driverFactory = DriverFactory() // Desktop DriverFactory doesn't need context

    // Create the Settings instance using PreferencesSettings for desktop
    // PreferencesSettings requires a java.util.prefs.Preferences delegate on JVM
    val preferencesDelegate = Preferences.userRoot() // Get the user's root preferences
    val settings = PreferencesSettings(preferencesDelegate) // Pass the delegate

    // --- Configure and Create HttpClient for Desktop ---
    // Attempt to read proxy settings from environment variables (common for CLI/Desktop)
    // Note: Setting JVM system properties is a common way for Java/Kotlin apps
    // to pick up proxy settings, especially when using engines like OkHttp
    // which often delegate to the JVM's default ProxySelector.
    val httpProxy = System.getenv("http_proxy") ?: System.getenv("HTTP_PROXY")
    val httpsProxy = System.getenv("https_proxy") ?: System.getenv("HTTPS_PROXY")

    // Set JVM system properties for proxy if environment variables are found
    if (!httpProxy.isNullOrBlank()) {
        try {
            val uri = URI(httpProxy)
            System.setProperty("http.proxyHost", uri.host)
            System.setProperty("http.proxyPort", uri.port.toString())
            // Note: Handling proxy authentication (user:pass@host:port) is more complex
            // and might require configuring OkHttp's Authenticator.
            // For now, we rely on basic host/port.
            System.setProperty("java.net.useSystemProxies", "true") // Encourage using system/JVM properties
            println("Using HTTP proxy from environment: $httpProxy") // Log for debugging
        } catch (e: Exception) {
            println("Warning: Could not parse http_proxy environment variable: $httpProxy. Error: ${e.message}")
        }
    }

    if (!httpsProxy.isNullOrBlank()) {
         try {
            val uri = URI(httpsProxy)
            System.setProperty("https.proxyHost", uri.host)
            System.setProperty("https.proxyPort", uri.port.toString())
             // Note: Handling proxy authentication is more complex
             System.setProperty("java.net.useSystemProxies", "true") // Encourage using system/JVM properties
             println("Using HTTPS proxy from environment: $httpsProxy") // Log for debugging
         } catch (e: Exception) {
             println("Warning: Could not parse https_proxy environment variable: $httpsProxy. Error: ${e.message}")
         }
    }

    // Create the HttpClient instance using the OkHttp engine
    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 10000 // Set request timeout to 10 seconds
        }
        // OkHttp engine should pick up JVM system properties set above
        // or the system's default ProxySelector if java.net.useSystemProxies is true.
        // This allows it to potentially use system-wide proxy settings or environment variables.
        // Explicit OkHttp proxy configuration would go here if needed, e.g.,
        // engine {
        //     config {
        //         // Example of explicit proxy config (usually not needed if system properties work)
        //         // proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress("your_proxy_host", 8080))
        //     }
        // }
    }
    // --- End HttpClient Configuration ---


    // Create the WordFetchApi instance with the configured client
    val wordFetchApi = WordFetchApi(client)


    Window(onCloseRequest = ::exitApplication, title = "WordBook") {
        // Pass the driver factory, settings instance, and WordFetchApi to the common App composable
        App(
            settings = settings,
            driverFactory = driverFactory,
            wordFetchApi = wordFetchApi // Pass the API instance
        )
    }
}
