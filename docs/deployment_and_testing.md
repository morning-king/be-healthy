# 部署与测试文档

## 1. 部署文档 (Deployment)

### 1.1 构建变体 (Build Variants)
本项目目前配置了以下构建类型：

*   **Debug**:
    *   `applicationId`: `com.behealthy.app`
    *   签名: 使用默认的 debug keystore。
    *   调试: 开启。
    *   Minify: 关闭。
*   **Release**:
    *   `applicationId`: `com.behealthy.app`
    *   签名: 需配置 release keystore。
    *   调试: 关闭。
    *   Minify: 开启 (R8 混淆)。

### 1.2 打包步骤
1.  **生成签名密钥** (首次):
    ```bash
    keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
    ```
2.  **配置签名信息**:
    在 `local.properties` 中配置密钥路径和密码（不提交到 Git），或在 CI/CD 环境变量中配置。
3.  **构建 APK**:
    ```bash
    ./gradlew assembleRelease
    ```
4.  **产物路径**:
    `app/build/outputs/apk/release/app-release.apk`

### 1.3 监控与告警
*   **当前状态**: 本地日志监控。
*   **日志工具**:
    *   内部封装 `AppLogger`，支持写入本地文件，方便在设备上直接查看日志（Debug 模式）。
    *   查看方式: `Profile` -> `Log Viewer`。

## 2. 测试文档 (Testing)

### 2.1 测试策略
由于项目处于快速迭代期，目前主要依赖**手工测试**，自动化测试框架已引入但尚未大规模覆盖。

#### 推荐测试分层
1.  **Unit Tests (JUnit 4/5)**:
    *   目标: ViewModel 业务逻辑, Repository 数据转换, Utility 函数。
    *   路径: `app/src/test/` (待补充)
2.  **UI Tests (Compose Test Rule)**:
    *   目标: 关键屏幕的渲染与交互 (e.g., `PlanCreationScreen`, `MoodTrackingScreen`)。
    *   路径: `app/src/androidTest/` (待补充)

### 2.2 核心功能测试用例 (Manual Test Cases)

| 模块 | 测试场景 | 预期结果 |
| :--- | :--- | :--- |
| **健身计划** | 创建新计划 (周模式) | 计划成功保存，且自动生成从开始到结束日期的每日任务。 |
| **健身计划** | 修改计划 (工作日配置) | 计划配置更新，但不影响已生成的历史任务。 |
| **每日打卡** | 点击完成任务 | 任务状态变更为 Completed，数据库记录更新。 |
| **心情追踪** | 录制语音备注 | 成功录制并保存音频文件，播放时声音正常。 |
| **数据同步** | 触发 Health Connect 同步 | 能够弹出权限申请，授权后能拉取到步数数据。 |

### 2.3 性能测试报告 (Performance)
*   **测试设备**: OPPO Find X6 Pro
*   **工具**: Android Studio Profiler

#### 关键指标基准
*   **冷启动时间**: < 1.5s
*   **列表滑动帧率**: 稳定 60fps
*   **内存占用**: 
    *   静默状态: ~150MB
    *   图表加载峰值: ~250MB

### 2.4 已知问题 (Known Issues)
*   **Health Connect**: 在部分未安装 Google Play 服务的设备上可能无法使用。
*   **后台同步**: 若设备开启强力省电模式，WorkManager 的周期性任务可能被推迟。
