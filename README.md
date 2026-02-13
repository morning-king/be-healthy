# BeHealthy

<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="Logo" width="80" height="80">
  <h3 align="center">BeHealthy</h3>
  <p align="center">
    一款专注于身心健康的 Android 应用程序
    <br />
    <a href="#features">功能特性</a> · <a href="#installation">安装部署</a> · <a href="#documentation">技术文档</a>
  </p>
</div>

## 📖 项目简介 (Introduction)

**BeHealthy** 是一款旨在帮助用户恢复和保持健康的综合性 Android 应用。它不仅仅是一个简单的健身打卡工具，更结合了心情追踪、健康数据同步和个性化计划管理，全方位关注用户的身心状态。

本项目采用现代 Android 开发技术栈构建（Kotlin + Jetpack Compose），遵循 Google 推荐的架构指南（Clean Architecture + MVVM），是一个优秀的 Android 现代化开发实践案例。

## ✨ 功能特性 (Features)

*   **😁 心情追踪 (Mood Tracking)**：
    *   记录每日心情，支持开心、悲伤、生气等多种状态。
    *   支持添加文字备注和语音录制。
    *   生成心情曲线，回顾情绪变化趋势。
*   **📅 健身计划 (Fitness Plan)**：
    *   自定义训练计划，支持按月、周、日设定。
    *   区分工作日和休息日，设置不同的饮食和运动目标。
    *   自动生成每日打卡任务。
*   **📊 数据统计 (Statistics)**：
    *   可视化的数据分析，包括步数、热量消耗、运动时长等趋势图。
    *   集成 Vico 图表库，提供流畅的交互体验。
*   **🗓️ 日历视图 (Calendar)**：
    *   直观展示每日打卡情况。
    *   集成农历和节假日显示。
    *   集成天气信息（OpenMeteo API）。
*   **🎨 多主题支持 (Theming)**：
    *   内置多种个性化主题（Wall-E, Doraemon, Tech, Nature, NBA, Snooker, Zen 等）。
    *   **[NEW]** 支持全局字体颜色模式切换 (浅色/深色/自动)，适配不同视觉需求。
    *   **[NEW]** 深度可定制的动态背景（如 Zen 旋转、赛博朋克粒子强度调节）。
    *   全应用配色动态切换，包括 Loading 动画。
    *   符合 WCAG 2.1 AA 无障碍标准（部分高对比度主题）。
*   **🔗 健康同步 (Health Connect)**：
    *   集成 Android Health Connect，自动同步步数、卡路里、距离等健康数据。
    *   支持 OPPO 设备传感器数据接入。

## 🛠️ 技术栈 (Tech Stack)

*   **语言**: [Kotlin](https://kotlinlang.org/) (1.9+)
*   **UI 框架**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material3)
*   **架构模式**: MVVM + Clean Architecture
*   **依赖注入**: [Hilt](https://dagger.dev/hilt/)
*   **本地存储**: [Room](https://developer.android.com/training/data-storage/room) (SQLite)
*   **异步处理**: Coroutines + Flow
*   **网络请求**: Retrofit + OkHttp
*   **图片加载**: Coil
*   **后台任务**: WorkManager
*   **健康数据**: Android Health Connect
*   **图表**: Vico

## 🚀 安装部署 (Installation)

### 环境要求
*   **Android Studio**: Ladybug | 2024.2.1 或更高版本
*   **JDK**: JDK 17
*   **Android SDK**: API Level 36 (Target), API Level 26 (Min)

### 部署步骤

1.  **克隆仓库**
    ```bash
    git clone https://github.com/morning-king/be-healthy.git
    cd be-healthy
    ```

2.  **配置环境**
    运行提供的设置脚本以确保环境正确：
    ```bash
    ./scripts/setup_dev_env.sh
    source ~/.zshrc # 或 source ~/.bash_profile
    ```

3.  **构建项目**
    使用 Android Studio 打开项目根目录，等待 Gradle Sync 完成。
    
    或者使用命令行构建：
    ```bash
    ./gradlew assembleDebug
    ```

4.  **运行应用**
    连接 Android 设备（需开启开发者模式）或启动模拟器，点击 Android Studio 的 **Run** 按钮。

    > **注意**: 由于集成了 Health Connect，建议在真机或安装了 Health Connect 应用的模拟器上运行。

## 📚 文档索引 (Documentation)

为了方便开发者深入了解项目，我们提供了详细的技术文档：

*   **[技术架构文档 (Architecture)](docs/technical_architecture.md)**: 系统的分层架构、核心模块说明。
*   **[技术设计文档 (Design)](docs/technical_design.md)**: 数据库设计、核心功能实现细节。
*   **[API 接口文档](docs/api_interface.md)**: 核心 Repository 接口及网络 API 说明。
*   **[数据库文档](docs/database_schema.md)**: 详细的表结构与字段定义。
*   **[综合趋势图表规范](docs/unified_trend_chart_spec.md)**: 运动数据可视化图表的设计与数据源规范。
*   **[内容库扩充报告](docs/content_expansion_report.md)**: 每日一言与诗词库的扩充统计及管理手册。
*   **[部署与测试文档](docs/deployment_and_testing.md)**: 详细的部署指南与测试策略。
*   **[版本更新日志](CHANGELOG.md)**: 版本迭代记录。

## 🤝 贡献指南 (Contributing)

欢迎提交 Issue 和 Pull Request！

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码 (`git commit -m 'feat: Add some feature'`)
4.  新建 Pull Request

**代码规范**:
*   提交前请运行 `./gradlew lint` 检查代码规范。
*   请遵循 Kotlin 官方编码风格。

## 📄 许可证 (License)

Distributed under the MIT License. See `LICENSE` for more information.

## 📞 联系方式

Project Link: [https://github.com/morning-king/be-healthy](https://github.com/morning-king/be-healthy)
