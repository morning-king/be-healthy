# BeHealthy APP 技术架构文档

## 1. 架构概览 (Architecture Overview)

本项目采用 **Clean Architecture** (整洁架构) 配合 **MVVM** (Model-View-ViewModel) 模式，旨在确保代码的**关注点分离**、**可测试性**和**可维护性**。

整体架构遵循 Google 推荐的现代 Android 应用架构指南。

### 1.1 分层架构图

```mermaid
graph TD
    UI[UI Layer (Presentation)] --> Data[Data Layer]
    Data --> Local[Local Data Source (Room)]
    Data --> Remote[Remote/System Data Source (Health Connect)]
```

*   **UI Layer**: 负责在屏幕上显示应用程序数据。
*   **Domain Layer**: (可选) 封装复杂的业务逻辑。目前主要逻辑位于 ViewModel 和 Repository 中。
*   **Data Layer**: 包含应用程序的业务逻辑并公开应用程序数据。

## 2. 核心分层详解

### 2.1 UI Layer (Presentation)
*   **职责**: 处理用户交互，观察数据变化并更新 UI。
*   **技术**: Jetpack Compose (声明式 UI), ViewModel, StateFlow。
*   **核心组件**:
    *   **Activity**: 单一 Activity (`MainActivity`) 作为容器。
    *   **Screens**: Composable 函数，代表不同的屏幕 (e.g., `PlanListScreen`, `MoodTrackingScreen`)。
    *   **ViewModels**: 保存和管理 UI 相关的状态，处理用户意图，通过 Repository 获取数据。
    *   **State**: 使用 `StateFlow` 或 `Compose State` 暴露不可变状态给 UI。

### 2.2 Data Layer
*   **职责**: 负责从不同数据源获取数据，并进行持久化。
*   **核心组件**:
    *   **Repositories**: 统一的数据访问入口，协调不同数据源。
        *   `FitnessPlanRepository`: 管理健身计划与任务生成。
        *   `DailyActivityRepository`: 管理每日运动数据同步。
        *   `MoodRepository`: 管理心情记录。
    *   **Data Sources**:
        *   **Local**: Room Database (SQLite) 用于持久化存储所有核心业务数据。
        *   **System/Remote**: 
            *   `HealthConnectManager`: 封装 Android Health Connect API，获取系统级健康数据（步数、卡路里等）。
            *   `OppoHealthService`: 针对特定设备的适配层（现已统一通过 Health Connect 接入）。

## 3. 技术栈 (Technology Stack)

| 类别 | 选型 | 版本 | 说明 |
| :--- | :--- | :--- | :--- |
| **编程语言** | Kotlin | 1.9+ | Android 首选开发语言 |
| **UI 框架** | Jetpack Compose | BOM 2026.01.01 | 现代原生 UI 工具包 |
| **架构组件** | Lifecycle, ViewModel | 2.10.0 | 管理 UI 生命周期和数据 |
| **依赖注入** | Hilt (Dagger) | 2.55 | 编译时依赖注入，解耦组件 |
| **异步处理** | Coroutines + Flow | Latest | 简化异步编程和响应式数据流 |
| **本地数据库** | Room | 2.8.4 | SQLite 对象映射库 |
| **后台任务** | WorkManager | 2.11.1 | 处理可靠的后台任务（同步、通知） |
| **图片加载** | Coil | 2.5.0 | 专为 Compose 设计的图片加载库 |
| **图表库** | Vico | 1.13.0 | 适用于 Compose 的轻量级图表库 |
| **健康数据** | Health Connect | 1.1.0 | Google 统一健康数据接口 |

## 4. 工程结构 (Project Structure)

```
com.behealthy.app
├── core                 // 核心基础模块
│   ├── database         // 数据库定义 (Entities, DAOs, RoomDatabase)
│   ├── network          // 网络/系统服务接口 (HealthConnectManager)
│   ├── designsystem     // 设计系统 (Theme, Colors, Typography)
│   ├── repository       // 数据仓库 (Repositories)
│   ├── worker           // 后台任务 (SyncWorker, WeeklyPlanWorker)
│   ├── notification     // 通知管理 (NotificationHelper)
│   └── util             // 通用工具类
├── feature              // 业务功能模块 (按功能垂直划分)
│   ├── splash           // 启动页
│   ├── plan             // 健身计划 (CRUD)
│   ├── task             // 任务执行 (Calendar, Detail)
│   ├── stats            // 数据统计
│   ├── mood             // 心情记录
│   └── profile          // 个人中心
├── di                   // Hilt 全局依赖注入模块
└── BeHealthyApp.kt      // Application 入口
```

## 5. 性能优化方案 (Performance Optimization)

### 5.1 UI 渲染优化
*   **Lazy Loading**: 列表界面（如计划列表、日历网格）均使用 `LazyColumn`/`LazyVerticalGrid`，确保仅渲染可见区域，减少内存占用。
*   **State Management**: 使用 `StateFlow` 和 `remember` 减少不必要的 Recomposition（重组）。
*   **Image Loading**: 使用 Coil 库进行异步图片加载，开启内存缓存和磁盘缓存，避免主线程阻塞。

### 5.2 数据处理优化
*   **异步数据库访问**: 所有 Room 数据库操作均通过 Coroutines (`suspend` functions) 或 Flow 在 `Dispatchers.IO` 线程执行，杜绝主线程 ANR。
*   **批量处理**: 在生成月度/周度任务时，使用 `insertAll` 批量插入 API，而非循环单条插入，显著提升写入速度。

### 5.3 启动速度优化
*   **Hilt Optimization**: 依赖注入采用按需加载，Singleton 组件仅在首次使用时初始化。
*   **Startup Profile**: (计划中) 引入 Android Baseline Profiles 优化冷启动时间。

### 5.4 电池与资源优化
*   **WorkManager**: 后台同步任务（如步数同步）使用 WorkManager 调度，利用系统优化机制（如 Doze mode），避免在低电量模式下频繁唤醒设备。
