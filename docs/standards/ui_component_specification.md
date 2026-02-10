# UI组件开发与布局完整性规避法则

## 1. 规避法则核心内容
**"标准组件优先与布局完整性原则"**

在修改核心UI组件（如导航栏、脚手架布局）时，必须严格遵循Material Design组件的Slot API（插槽规范）。对于标准悬浮元素（如FAB），优先使用`Scaffold`提供的专用`floatingActionButton`插槽。

**特殊情况例外**：当设计需求要求悬浮元素与`NavigationBar`的特定部位（如顶部边框）进行像素级精细对齐，且标准FAB位置无法满足时，允许使用**容器包裹（Container Wrapping）+ 覆盖层（Overlay）**模式，但严禁直接在`NavigationBar`内部强行插入非标准Item。

## 2. 适用场景
*   修改`Scaffold`、`NavigationBar`、`TopAppBar`等顶级容器组件时。
*   实现跨越层级或悬浮的UI元素时。
*   调整涉及屏幕适配（不同分辨率、屏幕密度）的布局代码时。

## 3. 实施步骤
1.  **需求分析**：确认UI变更是否可以通过标准属性（Parameters）实现。
2.  **API查阅**：查阅Jetpack Compose官方文档，确认组件是否支持预期的Slot。
3.  **模式选择**：
    *   **标准模式**：使用Scaffold FAB插槽。
    *   **覆盖模式**（特殊需求）：使用Box包裹NavigationBar，通过Alignment进行绝对定位。
4.  **最小侵入式修改**：优先使用组件自带参数。
5.  **布局隔离**：对于可能影响全局布局的改动，使用`Box`或`Surface`进行局部隔离测试。
6.  **回归验证**：修改后立即在预览（Preview）和真机上验证整体布局结构。

## 4. 代码示例

### ❌ 错误示范 (反面教材)
强行将FAB塞入NavigationBar，破坏RowScope的权重分配，导致布局塌陷。

```kotlin
// 错误：在NavigationBar中混合使用Item和Box，且未正确处理Weight
NavigationBar {
    NavigationBarItem(...) // Item 1
    NavigationBarItem(...) // Item 2
    
    // 错误：直接插入Box，未处理权重，可能导致挤压或布局错乱
    Box(modifier = Modifier.size(56.dp)) { ... } 
    
    NavigationBarItem(...) // Item 3
}
```

### ✅ 正确示范 A (标准FAB模式)
适用于标准Material Design设计，按钮悬浮在Tab栏上方或切入。

```kotlin
Scaffold(
    floatingActionButton = { Icon(...) },
    floatingActionButtonPosition = FabPosition.Center,
    bottomBar = {
        NavigationBar {
             // 配合 Spacer Item 使用
             NavigationBarItem(..., modifier = Modifier.weight(1f))
        }
    }
)
```

### ✅ 正确示范 B (覆盖层装饰模式 - 当前项目采用)
适用于需要精细控制装饰元素位置（如压住Tab栏顶线）的场景。

```kotlin
Scaffold(
    bottomBar = {
        // 使用Box作为容器
        Box(contentAlignment = Alignment.TopCenter) {
            // 1. 标准导航栏 (背景)
            NavigationBar {
                NavigationBarItem(...) // Item 1
                NavigationBarItem(...) // Item 2
                NavigationBarItem(...) // Item 3
                NavigationBarItem(...) // Item 4
            }
            
            // 2. 装饰覆盖层 (前景)
            // 独立于NavigationBar布局流，不会影响Tab均分
            ThemeMiddleDecoration(themeStyle)
        }
    }
)
```

## 5. 验证方法
1.  **视觉验证**：
    *   [ ] 检查所有Tab是否存在且位置正确。
    *   [ ] 检查悬浮元素是否遮挡了交互区域。
    *   [ ] 检查在不同屏幕尺寸下是否发生重叠或挤压。
2.  **结构验证**：
    *   使用Android Studio Layout Inspector检查组件层级，确认没有意外的嵌套。
3.  **自动化测试**：
    *   编写Compose UI Test，断言底部导航栏的节点数量（Node Count）等于预期值。

## 6. 责任人分配
*   **开发人员**：负责实施和自测，确保修改不破坏现有布局。
*   **Tech Lead**：负责代码审查（Code Review），拦截非标准的布局实现。
*   **QA**：负责在多机型上进行回归测试。

## 7. 执行时间表
*   **生效日期**：即日起（2026-02-10）。
*   **存量治理**：于本周内（2026-02-16前）扫描项目中的所有Scaffold使用处，修正非标准实现。

## 8. 培训计划
*   **文档同步**：将本文档发布至团队Wiki及代码仓库`docs/standards/`目录。
*   **例会宣讲**：在下一次晨会/周会中，用本次"底部栏消失事件"作为案例进行复盘宣讲。

## 9. 检查机制 & 10. 违规处理
*   **Code Review Check**：提交涉及`ui/`包的代码时，Reviewer必须检查`Scaffold`结构。
*   **CI/CD拦截**：集成UI自动化测试，若导航栏Item数量检测失败，禁止合并。
*   **违规处理**：
    *   初次违规：Code Review打回并要求阅读本规范。
    *   导致线上/主干故障：进行RCA（根因分析）并承担相应的Bug修复责任。

## 11. 强制执行 (CI/CR)
*   **Pre-commit Hook**：建议添加本地Hook，检查关键UI文件的修改。
*   **GitHub/GitLab Actions**：配置UI测试Pipeline，确保关键页面截图对比（Snapshot Testing）通过。
