# 解决 Compose Desktop 发布版本中的 NoSuchMethodError (Material 3 兼容性问题)

## 问题描述

在使用 Compose Multiplatform 开发 Android 和 Desktop 应用时，你可能会遇到一个棘手的 `NoSuchMethodError`。这个错误通常不会在你使用 Android Studio 的 Gradle 命令 `composeApp:run` 运行 Desktop 应用时出现，但在你构建发布版本（例如 Windows 的 `.exe` 文件）并尝试运行时就会触发。

错误信息通常指向 Compose 相关的类或方法，暗示着在运行时找不到某个预期的类或方法。

## 原因分析

经过排查，发现这个问题很可能是由于在 `commonMain` 源集中使用了 `androidx.compose.material3` 库导致的。尽管 `material3` 是 Compose UI 的最新推荐库，并且在 Android 上运行良好，但在当前的 Compose Desktop 版本中，直接在 `commonMain` 中依赖 `material3` 可能会导致兼容性问题，尤其是在构建发布版本时。

Compose Desktop 的实现可能与 `material3` 的某些内部机制或依赖项存在冲突，而这些冲突在开发环境（如 `composeApp:run`）中可能被 Gradle 或 IDE 的配置所掩盖，但在独立的发布包中则会暴露出来。

通过创建一个新的 Compose Multiplatform 项目，并逐步将原有项目的依赖项（特别是 `libs.versions.toml` 和 `composeApp/build.gradle.kts` 中的依赖）复制过去，然后通过注释和取消注释的方式进行隔离测试，最终可以定位到 `androidx.compose.material3` 相关的依赖是导致发布版本出现 `NoSuchMethodError` 的罪魁祸首。

具体来说，可能涉及在 `commonMain` 或 `compose.desktop.main` 块中对 `androidx.compose.material3` 的依赖引用。

## 解决方案

解决这个问题的根本方法是避免在 `commonMain` 中直接使用 `androidx.compose.material3` 库。你需要将所有对 `material3` 组件和 API 的引用替换为 `androidx.compose.material` 库中的对应项。

这通常意味着：

1.  **修改依赖项:** 从 `commonMain` 或 Desktop 目标集的依赖配置中移除所有 `androidx.compose.material3` 相关的依赖。
2.  **修改导入语句:** 将代码中所有 `import androidx.compose.material3.*` 的导入语句改为 `import androidx.compose.material.*`。
3.  **重写 UI 代码:** `material` 和 `material3` 库虽然功能相似，但在 API 设计和组件实现上存在差异。你需要根据 `material` 库的 API 重写使用 `material3` 组件的代码。这可能包括：
    *   按钮
    *   文本字段
    *   顶部应用栏
    *   底部导航栏
    *   对话框
    *   主题
    *   颜色系统
    *   排版系统
    *   形状系统
    *   等等...

这个过程可能需要对 UI 代码进行大量的重构，因为你需要将 `material3` 的概念（如 `ColorScheme`, `Typography`, `ShapeScheme`）映射到 `material` 的对应概念（如 `Colors`, `Typography`, `Shapes`），并调整组件的参数和用法。请查阅 Compose Material 库的官方文档以获取详细的 API 用法。

## 总结

`NoSuchMethodError` 在 Compose Desktop 发布版本中，尤其是在使用了 `material3` 的情况下，是一个已知的潜在问题。最可靠的解决方案是回退到使用 `androidx.compose.material` 库，并在 `commonMain` 中只使用与所有目标平台（包括 Desktop）兼容的 Compose UI 库版本。虽然这需要一些代码重构，但可以确保应用程序在发布后能够正常运行。

在未来的 Compose Multiplatform 版本中, `material3` 对 Desktop 的支持可能会得到改进，届时可以考虑迁移回 `material3`。但在当前阶段，使用 `material` 库是更稳妥的选择。
