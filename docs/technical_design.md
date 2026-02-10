# BeHealthy APP 技术详细设计文档

## 1. 概述
本文档主要描述 BeHealthy APP 的详细设计与核心功能实现逻辑。关于整体架构与技术栈，请参考 [技术架构文档](technical_architecture.md)。

## 2. 数据库设计 (Database Schema)

### 2.1 FitnessPlan (健身计划表)
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | Long (PK) | 自增主键 |
| name | String | 计划名称 |
| duration_type | Enum | MONTH, WEEK, DAY |
| target_text | String | 计划目标 |
| work_day_config | Embedded | 工作日配置 (饮食/运动开关及目标) |
| rest_day_config | Embedded | 非工作日配置 |
| status | Enum | ACTIVE, INACTIVE |
| created_at | Long | 创建时间戳 |
| updated_at | Long | 更新时间戳 |

### 2.2 FitnessTask (健身任务表)
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | Long (PK) | 自增主键 |
| plan_id | Long (FK) | 关联 FitnessPlan |
| date | String | 格式 "YYYY-MM-DD" |
| diet_record | Embedded | 早餐/午餐/晚餐 图片路径及热量 |
| exercise_work | Embedded | 工作日运动记录 (方式、时间、步数、截图等) |
| exercise_rest | Embedded | 非工作日运动记录 |
| is_completed | Boolean | 是否完成 |
| updated_at | Long | 最后更新时间 |

### 2.3 MoodRecord (心情记录表)
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| date | String (PK) | 日期 "YYYY-MM-DD" |
| mood | Enum | HAPPY, SAD, ANGRY, etc. |

### 2.4 DailyActivity (OPPO 运动同步表) [新增]
用于存储从 OPPO Health 或传感器同步的历史运动数据，确保无任务日期也能查看。

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| date | String (PK) | 日期 "YYYY-MM-DD" |
| steps | Int | 步数 |
| calories | Int | 消耗热量 (Kcal) |
| distance_meters | Int | 距离 (米) |
| duration_minutes | Int | 运动时长 (分) |
| updated_at | Long | 同步时间戳 |

## 3. 核心功能详细设计

### 3.1 多主题系统实现 (Theme System)
*   **设计目标**: 实现全应用风格统一切换，包括配色、Icon、Loading 动画及界面装饰。
*   **核心组件**:
    *   **ThemeStyle Enum**: 定义 `Default`, `WallE`, `Doraemon`, `Minions`, `Tech`, `NBA`, `Badminton`, `FootballWorldCup` 等枚举。
    *   **ThemeIcons.kt**: 集中管理所有主题图标的绘制逻辑（Canvas Drawing），实现图标与业务逻辑解耦。
        *   支持 `ThemeRotatingIcon`: 首页底部的动态旋转图标。
        *   支持 `ThemedIcon`: 导航栏 Tab 图标的静态/动态切换。
    *   **ThemeMiddleDecoration**: 底部导航栏的装饰覆盖层，支持自定义顶部线条（如哆啦A梦红丝带）和悬浮图标。
*   **Persistence**: 使用 DataStore 存储当前选中的 `theme_style`。
*   **Loading Animation**:
    *   组件: `RunningLoading.kt`
    *   机制: 基于 `Canvas` 的自定义绘制。不使用 Lottie 以减少包体积并增加动态控制能力。
    *   逻辑: 根据 `ThemeStyle` 参数分发到不同的 Draw 函数 (e.g., `drawWallE`, `drawDoraemon`)。
    *   动画: 使用 `rememberInfiniteTransition` 实现上下浮动 (Bobbing) 和跑步循环 (Run Cycle)。

### 3.2 历史数据同步机制 (Historical Sync)
*   **组件**: `SyncWorker` (WorkManager)
*   **策略**:
    *   **周期**: 每 15 分钟 (最小间隔) 或应用启动时触发。
    *   **范围**: 每次同步会检查并同步**最近 90 天**的数据。
    *   **Mock 逻辑 (OppoHealthService)**:
        *   为了模拟真实稳定的历史数据，使用 `date.hashCode()` 作为 `Random` 种子。
        *   保证同一日期多次请求返回相同的数据，但不同日期数据不同。
*   **流程**:
    1.  计算起始日期 (`LocalDate.now().minusMonths(3)`).
    2.  循环遍历至今日。
    3.  调用 `OppoHealthService.getDailyActivity(date)`.
    4.  写入/更新 `DailyActivityEntity`.

### 3.3 日历空状态逻辑 (Calendar Empty State)
*   **场景**: 用户点击日历上的某一天，该日无 `FitnessTask`。
*   **逻辑**:
    *   查询 `TaskViewModel` 中的 `dailyActivityForSelectedDate`。
    *   如果存在同步数据 (`DailyActivityEntity`)，则展示四宫格数据视图 (步数/消耗/距离/时长)。
    *   如果不存在且是今日，展示实时数据。
    *   如果既无任务也无同步数据，显示“无数据”空状态。

### 3.4 自动任务生成
*   监听 `FitnessPlan` 的状态变化。
*   使用 `WorkManager` 每日凌晨 (00:00) 检查处于 `ACTIVE` 状态的计划，并生成当天的 `FitnessTask`。

### 3.5 个人资料与心情语音 (UserProfile & Mood Audio)
*   **UserProfile**:
    *   扩展 `UserProfileDataSource`，增加 `NOTE_IMAGE_URI` 支持个人简介图片。
    *   UI: `ProfileScreen` 支持图片选择与裁剪预览。
*   **Mood Audio**:
    *   `MoodRecordEntity` 增加音频路径字段。
    *   `MoodTrackingScreen` 集成录音与播放功能。

### 3.6 通知系统 (Notification System)
*   **设计目标**: 提高用户留存，定期推送健康报告与计划。
*   **实现方案**:
    *   **NotificationHelper**: 封装通知渠道创建 (Channel ID: `be_healthy_channel`) 与发送逻辑。
    *   **Worker**:
        *   `WeeklyReportWorker`: 每周日 22:00 触发，发送周报通知。
        *   `WeeklyPlanWorker`: 每周一 10:00 触发，发送新一周计划提醒。
    *   **WorkManager调度**: 使用 `PeriodicWorkRequestBuilder` 配合动态 `InitialDelay` 计算，确保在指定时间点准时触发。

### 3.7 统计分析增强 (Statistics Enhancements)
*   **多维数据筛选**: 支持最近一周、一月、三月、半年、今年及**自定义时间范围**的数据筛选。
*   **智能分析文案**:
    *   `generateExerciseAnalysis`: 基于前后半段数据的趋势对比 (Trend Analysis)，生成鼓励或调整建议。
    *   `generateMoodAnalysis`: 基于心情评分 (1-5分) 的平均值与分布，生成心理健康建议。
*   **可视化升级**:
    *   新增 **PieChart (饼图)** 展示心情分布。
    *   支持在条形图与饼图之间切换。
    *   OPPO 运动健康数据 (Steps, Calories) 与应用内手动记录数据合并展示。

## 4. 关键算法与策略

### 4.1 数据一致性
*   采用 `Room` 的 `Flow` 观察者模式。
*   SyncWorker 只负责写库，ViewModel 只负责读库。
*   UI 自动响应数据库变化，无需手动回调更新。

### 4.2 异常处理
*   **同步失败**: WorkManager 配置 `BackoffPolicy.EXPONENTIAL` 自动重试。
*   **权限缺失**: 自动降级为 Mock 数据或提示用户。
