<a name="readme-top"></a>

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <h3 align="center">BeHealthy</h3>

  <p align="center">
    一款专注于身心健康的 Android 应用程序
    <br />
    <br />
    <a href="#usage">查看功能</a>
    ·
    <a href="#roadmap">路线图</a>
    ·
    <a href="#contributing">贡献</a>
  </p>
</div>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>目录</summary>
  <ol>
    <li>
      <a href="#about-the-project">关于项目</a>
      <ul>
        <li><a href="#built-with">技术栈</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">快速开始</a>
      <ul>
        <li><a href="#prerequisites">环境要求</a></li>
        <li><a href="#installation">安装步骤</a></li>
      </ul>
    </li>
    <li><a href="#usage">功能使用</a></li>
    <li><a href="#roadmap">路线图</a></li>
    <li><a href="#contributing">贡献指南</a></li>
    <li><a href="#license">许可证</a></li>
    <li><a href="#contact">联系方式</a></li>
  </ol>
</details>

<!-- ABOUT THE PROJECT -->
## 关于项目

**BeHealthy** 是一款旨在帮助用户恢复和保持健康的综合性 Android 应用。它不仅仅是一个简单的健身打卡工具，更结合了心情追踪、健康数据同步和个性化计划管理，全方位关注用户的身心状态。

主要特点：
*   **心情追踪**：记录每日心情，支持文字备注和语音录制，生成心情曲线。
*   **健身计划**：自定义训练计划，支持区分工作日和休息日的不同目标（饮食/运动）。
*   **数据统计**：可视化的数据分析，包括步数、热量消耗、运动时长等趋势图。
*   **日历视图**：直观展示每日打卡情况，集成天气信息（OpenMeteo）。
*   **多主题支持**：内置多种个性化主题（Wall-E, Doraemon, Tech, Nature 等）。
*   **健康同步**：集成 Android Health Connect，自动同步步数和健康数据。

<p align="right">(<a href="#readme-top">back to top</a>)</p>

### 技术栈

本项目采用现代 Android 开发技术栈构建：

*   [![Kotlin][Kotlin-badge]][Kotlin-url]
*   [![Compose][Compose-badge]][Compose-url]
*   [![Hilt][Hilt-badge]][Hilt-url]
*   [![Room][Room-badge]][Room-url]
*   **Health Connect**
*   **WorkManager**
*   **Retrofit** & **OkHttp**

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- GETTING STARTED -->
## 快速开始

请按照以下步骤在本地设置和运行项目。

### 环境要求

*   Android Studio (推荐最新版本，支持 SDK 36)
*   JDK 17 或更高版本
*   Android SDK API Level 36 (项目 Target SDK)

### 安装步骤

1.  克隆仓库
    ```sh
    git clone https://github.com/morning-king/be-healthy.git
    ```
2.  使用 Android Studio 打开项目目录
3.  等待 Gradle Sync 完成
    *   项目使用了 Gradle Version Catalog，依赖会自动下载。
4.  运行应用
    *   连接 Android 设备或启动模拟器（推荐 API 26+）。
    *   点击 Android Studio 的 "Run" 按钮。

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- USAGE EXAMPLES -->
## 功能使用

*   **首页/日历**：查看当月打卡记录，点击日期记录心情或查看详情。
*   **计划页**：创建新的健身计划，设置每日目标。
*   **统计页**：查看周/月维度的运动和心情数据分析。
*   **个人中心**：切换应用主题，查看应用日志。

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- ROADMAP -->
## 路线图

- [x] 基础健身打卡功能
- [x] 心情追踪（支持语音）
- [x] 数据统计图表
- [x] Health Connect 集成
- [x] 多主题切换
- [ ] 云端数据同步
- [ ] 社交分享功能
- [ ] AI 健康建议

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- CONTRIBUTING -->
## 贡献指南

贡献是开源社区的核心。如果您有好的建议，欢迎 Fork 本仓库并提交 Pull Request。

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- LICENSE -->
## 许可证

Distributed under the MIT License. See `LICENSE` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- CONTACT -->
## 联系方式

Project Link: [https://github.com/morning-king/be-healthy](https://github.com/morning-king/be-healthy)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- MARKDOWN LINKS & IMAGES -->
[Kotlin-badge]: https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white
[Kotlin-url]: https://kotlinlang.org/
[Compose-badge]: https://img.shields.io/badge/Jetpack%20Compose-2026.01-4285F4?style=for-the-badge&logo=android&logoColor=white
[Compose-url]: https://developer.android.com/jetpack/compose
[Hilt-badge]: https://img.shields.io/badge/Hilt-2.55-2E7D32?style=for-the-badge&logo=google&logoColor=white
[Hilt-url]: https://dagger.dev/hilt/
[Room-badge]: https://img.shields.io/badge/Room-2.8.4-4285F4?style=for-the-badge&logo=sqlite&logoColor=white
[Room-url]: https://developer.android.com/training/data-storage/room
