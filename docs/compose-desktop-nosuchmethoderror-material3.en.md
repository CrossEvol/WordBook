# Resolving NoSuchMethodError in Compose Desktop Release Builds (Material 3 Compatibility Issue)

## Problem Description

When developing Android and Desktop applications using Compose Multiplatform, you might encounter a tricky `NoSuchMethodError`. This error typically does not appear when you run the Desktop application using the Android Studio Gradle command `composeApp:run`, but it triggers when you build a release version (e.g., a Windows `.exe` file) and try to run it.

The error message usually points to Compose-related classes or methods, indicating that a certain expected class or method cannot be found at runtime.

## Cause Analysis

Upon investigation, it's highly probable that this issue is caused by using the `androidx.compose.material3` library in the `commonMain` source set. Although `material3` is the latest recommended library for Compose UI and works well on Android, directly depending on `material3` in `commonMain` can lead to compatibility issues in the current Compose Desktop version, especially when building release versions.

The Compose Desktop implementation might have conflicts with certain internal mechanisms or dependencies of `material3`. These conflicts might be masked by Gradle or IDE configurations in the development environment (like `composeApp:run`) but become apparent in a standalone release package.

By creating a new Compose Multiplatform project and gradually copying dependencies from the original project (specifically from `libs.versions.toml` and `composeApp/build.gradle.kts`), then performing isolation tests by commenting and uncommenting dependencies, you can eventually pinpoint the `androidx.compose.material3`-related dependencies as the culprit causing the `NoSuchMethodError` in release builds.

Specifically, this might involve dependency references to `androidx.compose.material3` within the `commonMain` or `compose.desktop.main` blocks.

## Solution

The fundamental solution to this problem is to avoid using the `androidx.compose.material3` library directly in `commonMain`. You need to replace all references to `material3` components and APIs with their counterparts from the `androidx.compose.material` library.

This typically means:

1.  **Modify Dependencies:** Remove all `androidx.compose.material3`-related dependencies from the dependency configurations of `commonMain` or the Desktop target.
2.  **Modify Import Statements:** Change all `import androidx.compose.material3.*` import statements in your code to `import androidx.compose.material.*`.
3.  **Rewrite UI Code:** While the `material` and `material3` libraries have similar functionalities, they differ in API design and component implementation. You will need to rewrite the code that uses `material3` components based on the `material` library's API. This may include:
    *   Buttons
    *   Text Fields
    *   Top App Bars
    *   Bottom Navigation Bars
    *   Dialogs
    *   Themes
    *   Color Systems
    *   Typography Systems
    *   Shape Systems
    *   Etc...

This process may require significant refactoring of your UI code, as you'll need to map `material3` concepts (like `ColorScheme`, `Typography`, `ShapeScheme`) to their `material` equivalents (like `Colors`, `Typography`, `Shapes`) and adjust component parameters and usage. Please refer to the official documentation for the Compose Material library for detailed API usage.

## Summary

`NoSuchMethodError` in Compose Desktop release builds, especially when using `material3`, is a known potential issue. The most reliable solution is to revert to using the `androidx.compose.material` library and only use Compose UI library versions in `commonMain` that are compatible with all target platforms, including Desktop. While this requires some code refactoring, it ensures that the application runs correctly after release.

In future versions of Compose Multiplatform, `material3` support for Desktop may improve, at which point migrating back to `material3` can be considered. However, at this stage, using the `material` library is a more stable choice.
