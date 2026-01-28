# BeHealthy App 运行指南

## 1. 项目状态

✅ **构建状态**: 成功 (Build Successful)
✅ **核心功能**:
*   **健身计划管理**: 创建、查看计划。
*   **每日打卡**: 日历视图查看每日任务，点击日期查看任务列表并打卡。
*   **心情记录**: 记录每日心情评分和笔记，查看历史记录。
*   **每日一悟**: 首页展示道德经名言。
*   **数据同步**: 基础框架已搭建 (WorkManager)，目前为模拟同步。
*   **统计分析**: 界面占位符已添加。

## 2. 如何运行项目

### 步骤 1: 使用 Android Studio 打开
1.  启动 **Android Studio**。
2.  选择 `Open`，然后导航到 `/Users/mac/Documents/workspace/be-healthy` 目录并点击打开。
3.  等待 Android Studio 完成 **Gradle Sync**。由于我们已经修复了 Gradle 配置，这步应该会顺利完成。

### 步骤 2: 配置模拟器 (解决下载失败问题)
如果你在 Android Studio 中通过 Device Manager 创建模拟器时遇到下载 System Image 失败的问题（通常是因为网络代理原因），请尝试以下手动步骤：

1.  **手动下载镜像**:
    *   复制之前的下载链接 (例如 `https://dl.google.com/android/repository/sys-img/google_apis_playstore/x86_64-36.1_r04.zip`) 到浏览器中下载。
    *   或者使用我们提供的脚本尝试辅助安装（如果网络环境允许）：`./scripts/manual_install_sys_img.sh`

2.  **手动安装**:
    *   找到 Android SDK 目录 (通常在 `/Users/mac/Library/Android/sdk`)。
    *   进入 `system-images/android-34/google_apis_playstore/x86_64/` 目录（如果没有则创建）。
    *   将下载的 zip 包解压到该目录下。
    *   重启 Android Studio，再次打开 Device Manager，它应该能识别已安装的镜像。

### 步骤 3: 运行 App
1.  在 Android Studio 顶部工具栏，选择一个模拟器（例如 `Pixel_6_Pro_API_34`）。
2.  点击绿色的 **Run** 按钮 (三角形图标)。
3.  等待应用在模拟器中启动。

## 3. 常见问题排查

*   **Gradle 错误**: 如果再次遇到 Gradle 相关错误，请尝试在终端运行 `./gradlew clean assembleDebug` 来清理并重新构建。
*   **KAPT 错误**: 我们已经通过配置 JVM 参数解决了 JDK 17+ 的兼容性问题。如果问题复发，请确保 `gradle.properties` 中的 `--add-opens` 参数未被修改。

## 4. 下一步开发计划
*   集成真实的 OPPO Health SDK 进行数据同步。
*   完善统计分析界面的图表显示。
*   优化 UI 细节动画。
