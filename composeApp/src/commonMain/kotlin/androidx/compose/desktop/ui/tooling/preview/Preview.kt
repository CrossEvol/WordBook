package androidx.compose.desktop.ui.tooling.preview

/**
 * This is a trick to enable desktop previews in Android Studio/IntelliJ IDEA
 * by providing an expect annotation in the common source set that resolves
 * to the desktop preview annotation in the desktop source set.
 *
 * The Android tooling will typically ignore this expect annotation and use
 * the standard Android preview annotation from androidx.compose.ui.tooling.preview.
 *
 * Requires the "Compose Multiplatform IDE Support" plugin.
 */
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation // Allows platforms to not provide an actual if they don't support it
expect annotation class Preview()
