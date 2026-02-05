# 全面优化与重构计划 (全指标覆盖版)

## 1. 健身页面日历与小结优化
*   **日历视图**: 
    *   重构 `CalendarScreen`，配置 `DayBinder` 仅渲染 `DayPosition.MonthDate` (当月日期)，隐藏非本月日期。
    *   添加 `LaunchedEffect` 监听日历滑动，实时更新 `TaskViewModel.currentMonth`。
*   **月度小结**: 
    *   修改 `FitnessMonthlyStats`，使其基于动态的 `viewModel.currentMonth` 计算当月数据。

## 2. 健身任务录入光标修复
*   **修复方案**: 在 `TaskDetailItem` 中，将输入框状态从 `String` 升级为 `TextFieldValue`，并优化 `onValueChange` 逻辑，确保在输入数字时光标位置不丢失。

## 3. 心情录入 UI 优化
*   **UI 调整**: 将 `MoodEntryDialog` 中的“确定”按钮背景色强制设为 `MaterialTheme.colorScheme.primary` (深色)。

## 4. 统计页面数据源重构 (全指标覆盖)
*   **核心逻辑 (`StatisticsViewModel`)**:
    *   **移除**: 删除 `HealthConnectManager` 和 `DailyActivityRepository`。
    *   **重写聚合算法**: 仅使用 `FitnessTaskDao` 数据。
*   **指标更新清单 (确保以下所有项均更新)**:
    1.  **总运动天数** (&下钻列表): 仅统计有完成任务日期的天数。
    2.  **总消耗热量** (&下钻列表): SUM(已完成任务的 `actualCalories`)。
    3.  **总运动时长** (&下钻列表): SUM(已完成任务的 `actualMinutes`)。
    4.  **总步数** (&下钻列表): SUM(已完成任务的 `actualSteps`)。
    5.  **日均数据**: (日均热量/时长/步数) = 总量 / 运动天数。
    6.  **运动趋势图**: 数据点完全基于上述每日汇总数据。
    7.  **运动分析文案**: 基于新的手动数据量级生成建议。
    8.  **计划详情**: 确保计划维度的“总消耗热量”也使用任务的 `actualCalories` 累加。

## 5. 统计图表交互增强 (点击详情)
*   **交互实现**:
    *   在 `ExerciseCurveChart` 和 `MoodCurveChart` 增加点击手势监听。
    *   **悬浮层 (Overlay)**:
        *   **心情**: 显示 Emoji + 备注。
        *   **运动**: 显示当日任务摘要 (如 "跑步 5km, 游泳 30min")。
        *   **视觉**: 绘制垂直辅助线。
