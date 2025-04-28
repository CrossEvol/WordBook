package androidx.compose.desktop.ui.tooling.preview

/**
 * Actual implementation of the Preview annotation for desktop.
 * 
 * This is used by the Compose Multiplatform IDE Support plugin to 
 * render previews in the IDE for desktop targets.
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CLASS
)
actual annotation class Preview 