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
