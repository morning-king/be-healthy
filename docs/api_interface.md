# API 接口文档

## 1. 概述
本文档详细描述了 BeHealthy 应用中涉及的外部网络 API 接口以及核心内部 Repository 接口。

## 2. 外部网络 API (External Network APIs)

### 2.1 天气预报 API (OpenMeteo)
*   **Base URL**: `https://api.open-meteo.com/`
*   **Interface**: `WeatherApi`

#### 获取天气预报
*   **Endpoint**: `GET /v1/forecast`
*   **描述**: 根据经纬度获取未来几天的天气预报。
*   **请求参数 (Request Parameters)**:

| 参数名 | 类型 | 必选 | 说明 | 示例 |
| :--- | :--- | :--- | :--- | :--- |
| `latitude` | Double | 是 | 纬度 | `39.9042` |
| `longitude` | Double | 是 | 经度 | `116.4074` |
| `daily` | String | 否 | 请求的数据字段 | `"weather_code,temperature_2m_max,temperature_2m_min"` |
| `timezone` | String | 否 | 时区 | `"auto"` |
| `start_date` | String | 否 | 开始日期 (YYYY-MM-DD) | `"2023-10-01"` |
| `end_date` | String | 否 | 结束日期 (YYYY-MM-DD) | `"2023-10-07"` |

*   **响应格式 (Response)**: JSON
    ```json
    {
      "daily": {
        "time": ["2023-10-01", "2023-10-02"],
        "weather_code": [0, 1],
        "temperature_2m_max": [25.5, 24.0],
        "temperature_2m_min": [15.0, 14.5]
      }
    }
    ```

### 2.2 中国天气 API (WeatherCn)
*   **Base URL**: `http://www.weather.com.cn/`
*   **Interface**: `WeatherCnApi`

#### 获取实时天气
*   **Endpoint**: `GET /data/sk/{cityCode}.html`
*   **描述**: 根据城市代码获取实时天气信息。
*   **请求参数**:

| 参数名 | 类型 | 必选 | 说明 | 示例 |
| :--- | :--- | :--- | :--- | :--- |
| `cityCode` | String | 是 | 城市代码 | `"101010100"` (北京) |

*   **响应格式**: JSON
    ```json
    {
      "weatherinfo": {
        "city": "北京",
        "cityid": "101010100",
        "temp": "18",
        "WD": "南风",
        "WS": "2级",
        "SD": "30%",
        "time": "10:00"
      }
    }
    ```

## 3. 内部核心 API (Internal Repository APIs)

### 3.1 健身计划仓库 (FitnessPlanRepository)
负责管理用户的健身计划及其生成的每日任务。

#### `createPlan(plan: FitnessPlanEntity): Long`
*   **描述**: 创建一个新的健身计划，并自动生成该计划周期内的所有每日任务。
*   **参数**:
    *   `plan`: 包含计划详情的实体对象。
*   **返回值**: 新创建计划的 ID。
*   **逻辑**:
    1.  插入 Plan 到数据库。
    2.  遍历 `startDate` 到 `endDate`。
    3.  根据工作日/休息日配置，生成 `FitnessTaskEntity`。

#### `generateTasksForPlan(plan: FitnessPlanEntity)`
*   **描述**: (Private) 根据计划配置生成每日任务的核心逻辑。
*   **规则**:
    *   仅在 `DietEnabled` 或 `ExerciseEnabled` 为 true 的日期生成任务。

### 3.2 每日活动仓库 (DailyActivityRepository)
负责管理从 Health Connect 或设备传感器同步的步数、卡路里等数据。

#### `getDailyActivity(date: String): Flow<DailyActivityEntity?>`
*   **描述**: 获取指定日期的活动数据流。
*   **参数**: `date` (Format: "YYYY-MM-DD")
*   **返回值**: `Flow` 发射 `DailyActivityEntity` 或 `null`。

### 3.3 健康连接管理器 (HealthConnectManager)
封装 Android Health Connect SDK 的交互。

#### `getDailyActivity(date: LocalDate): DailyActivityData?`
*   **描述**: 聚合查询指定日期的健康数据。
*   **参数**: `date` (LocalDate)
*   **返回值**: `DailyActivityData` (包含 steps, calories, distance) 或 `null` (若无权限/出错)。
*   **权限要求**: 需要 `READ_STEPS`, `READ_DISTANCE`, `READ_TOTAL_CALORIES` 等权限。
