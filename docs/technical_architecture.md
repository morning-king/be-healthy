# BeHealthy APP 技术架构文档

## 1. 架构概览 (Architecture Overview)

本项目采用 **Clean Architecture** (整洁架构) 配合 **MVVM** (Model-View-ViewModel) 模式，旨在确保代码的**关注点分离**、**可测试性**和**可维护性**。

整体架构遵循 Google 推荐的现代 Android 应用架构指南。

### 1.1 分层架构图

```
UI Layer (Presentation)
   ▼
Domain Layer (Optional)
   ▼
Data Layer
```

*   **UI Layer**: 负责在屏幕上显示应用程序数据。
*   **Domain Layer**: 封装复杂的业务逻辑（本项目根据复杂度按需使用）。
*   **Data Layer**: 包含应用程序的业务逻辑并公开应用程序数据。

## 2. 核心分层详解

### 2.1 UI Layer (Presentation)
*   **职责**: 处理用户交互，观察数据变化并更新 UI。
*   **技术**: Jetpack Compose (声明式 UI), ViewModel, StateFlow。
*   **核心组件**:
    *   **Activity**: 单一 Activity (`MainActivity`) 作为容器。
    *   **Screens**: Composable 函数，代表不同的屏幕。
    *   **ViewModels**: 保存和管理 UI 相关的状态，处理用户意图，通过 Repository 获取数据。
    *   **State**: 使用 `StateFlow` 或 `Compose State` 暴露不可变状态给 UI。

### 2.2 Data Layer
*   **职责**: 负责从不同数据源获取数据，并进行持久化。
*   **核心组件**:
    *   **Repositories**: 统一的数据访问入口，协调不同数据源。
    *   **Data Sources**:
        *   **Local**: Room Database (SQLite) 用于持久化存储。
        *   **Remote/System**: OPPO Health Service (模拟/实际传感器) 用于获取步数等数据。

## 3. 技术栈 (Technology Stack)

| 类别 | 选型 | 版本 (参考) | 说明 |
| :--- | :--- | :--- | :--- |
| **编程语言** | Kotlin | 1.9+ | Android 首选开发语言 |
| **UI 框架** | Jetpack Compose | BOM 2024.x | 现代原生 UI 工具包 |
| **架构组件** | Lifecycle, ViewModel | Latest | 管理 UI 生命周期和数据 |
| **依赖注入** | Hilt (Dagger) | 2.50 | 编译时依赖注入，解耦组件 |
| **异步处理** | Coroutines + Flow | Latest | 简化异步编程和响应式数据流 |
| **本地数据库** | Room | 2.6.1 | SQLite 对象映射库 |
| **后台任务** | WorkManager | 2.9.0 | 处理可靠的后台任务（同步、通知） |
| **图片加载** | Coil | 2.5.0 | 专为 Compose 设计的图片加载库 |
| **图表库** | Vico | 1.13.0 | 适用于 Compose 的轻量级图表库 |

## 4. 工程结构 (Project Structure)

```
com.behealthy.app
├── core                 // 核心基础模块，被 Feature 模块依赖
│   ├── database         // 数据库定义 (Entities, DAOs, RoomDatabase)
│   ├── network          // 网络/系统服务接口 (OppoHealthService)
│   ├── designsystem     // 设计系统 (Theme, Colors, Typography, Components)
│   ├── worker           // 后台任务 (SyncWorker, DailyTaskWorker, WeeklyReport/PlanWorker)
│   ├── notification     // 通知管理 (NotificationHelper)
│   └── util             // 通用工具类
├── feature              // 业务功能模块 (按功能垂直划分)
│   ├── splash           // 启动页
│   ├── plan             // 健身计划 (CRUD)
│   ├── task             // 任务执行 (Calendar, Detail)
│   ├── stats            // 数据统计
│   ├── mood             // 心情记录
│   └── profile          // 个人中心 (Theme Switch)
├── di                   // Hilt 全局依赖注入模块
└── BeHealthyApp.kt      // Application 入口
```

## 5. 关键设计模式

### 5.1 单一数据源 (Single Source of Truth)
Repository 负责合并来自 API、系统传感器的数据，并统一存储到 Room 数据库。UI 层只观察 Room 数据库的数据变化，确保 UI 展示的数据始终一致。

### 5.2 单向数据流 (UDF)
1.  **Event**: 用户操作产生事件，传递给 ViewModel。
2.  **Update**: ViewModel 处理业务逻辑，更新 State。
3.  **Display**: UI 观察 State 变化并重绘。

### 5.3 依赖注入 (Dependency Injection)
使用 Hilt 将 Repository 注入 ViewModel，将 DAO 注入 Repository。这使得代码解耦，易于替换实现（例如在测试中使用 Fake Repository）。
