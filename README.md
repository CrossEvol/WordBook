This is a Kotlin Multiplatform project targeting Android, Desktop.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

## Building Desktop Release

You can build native desktop installers for different operating systems. Run the following commands in the project root directory:

*   **Build all formats (MSI, DMG, Deb):**
    ```bash
    ./gradlew packageDistributionForCurrentOS
    ```

*   **Build Windows Installer (.msi):**
    ```bash
    ./gradlew packageMsi
    ```

*   **Build macOS Installer (.dmg):**
    ```bash
    ./gradlew packageDmg
    ```

*   **Build Linux Installer (.deb):**
    ```bash
    ./gradlew packageDeb
    ```

The output files will be located in the `composeApp/build/compose/binaries/main/app` directory (or a similar path within `composeApp/build/compose/`).

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

## Building Android Release APK

To build a release version of the Android application package (APK), follow these steps:

1.  **Open a terminal or command prompt** in the root directory of your project (where `gradlew` is located).
2.  **Run the Gradle task** for assembling the release build of the `composeApp` module:

    ```bash
    ./gradlew :composeApp:assembleRelease
    ```

    *   This command tells Gradle to execute the `assembleRelease` task specifically for the `composeApp` module.
    *   The `assembleRelease` task compiles the code, processes resources, and packages everything into a release APK. By default, when no signing configuration is provided, this APK is signed with a debug signing key provided by the Android SDK, resulting in a file like `composeApp-release-unsigned.apk`. If debug signing is implicitly used, it might be named `composeApp-release.apk`.
3.  **Find the generated APK:** The resulting release APK file will be located in the `composeApp/build/outputs/apk/release/` directory.

**Signing the Release APK for Production**

For a production release to the Google Play Store or other app stores, you **must** sign your APK (or App Bundle) with your own unique release signing key. The APK generated by `assembleRelease` without explicit signing configuration is **not** suitable for production.

Here's a summary of the process:

1.  **Generate a Signing Key:** If you don't already have one, create a keystore file using the `keytool` command-line utility provided with the Java Development Kit (JDK).
    ```bash
    keytool -genkeypair -v -keystore my-release-key.keystore -alias my-alias -keyalg RSA -keysize 2048 -validity 10000
    ```
    Replace `my-release-key.keystore` and `my-alias` with your desired names. You will be prompted to set passwords for the keystore and the key alias. **Keep this keystore file and its passwords secure and backed up!** Losing it means you cannot update your app on stores like Google Play.

    *   You might need to encode your keystore file, for example, if you are using CI/CD pipelines that require secrets to be stored as strings. You can use `base64` for this:
        ```bash
        base64 my-release-key.keystore > keystore_base64.txt
        ```
        This command encodes the binary keystore file into a base64 string and saves it to `keystore_base64.txt`. You would then store the content of `keystore_base64.txt` securely (e.g., as a CI/CD secret) and decode it back to a file during the build process.

2.  **Configure Signing in `build.gradle.kts`:** Add a `signingConfigs` block within the `android` block in your `composeApp/build.gradle.kts` file. Reference your keystore file and provide the keystore password, key alias, and key password (it's recommended to store passwords securely, e.g., in environment variables or a separate properties file, rather than directly in the build script).
    ```kotlin
    android {
        // ... other android configurations ...
        signingConfigs {
            create("release") {
                storeFile = file("path/to/your/my-release-key.keystore") // Use file()
                storePassword = System.getenv("KEYSTORE_PASSWORD") // Example using environment variable
                keyAlias = "my-alias"
                keyPassword = System.getenv("KEY_PASSWORD") // Example using environment variable
            }
        }
        buildTypes {
            getByName("release") {
                isMinifyEnabled = false // Or true for obfuscation
                signingConfig = signingConfigs.getByName("release") // Apply the release signing config
            }
        }
    }
    ```
3.  **Build with Signing Config:** After configuring `build.gradle.kts`, running `./gradlew :composeApp:assembleRelease` will now produce a signed APK (`composeApp-release.apk`) in the `composeApp/build/outputs/apk/release/` directory, ready for distribution.

For more detailed information on signing, refer to the official Android documentation.
