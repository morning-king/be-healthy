# BeHealthy 项目开发规范与 AI 规则

本文件定义了 BeHealthy 项目的开发规范，AI 助手在协助开发时必须严格遵守。

## 1. 代码质量与流程 (Workflow)

### 1.1 验证流程 (Mandatory)
*   **Lint 检查**: 在提交代码前，必须运行 `./gradlew lint` 并修复所有 Error 级别的警告。
*   **编译构建**: 任何代码修改后，必须确保 `./gradlew assembleDebug` 编译通过。
*   **交付标准**: 任务完成时，必须保证：
    1.  代码静态分析无严重问题。
    2.  项目编译构建成功。
    3.  核心功能运行正常（如涉及）。

### 1.2 Git 提交规范
遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：
*   `feat: 新增功能描述`
*   `fix: 修复 bug 描述`
*   `docs: 文档修改`
*   `style: 代码格式修改 (不影响逻辑)`
*   `refactor: 代码重构`
*   `chore: 构建过程或辅助工具变动`

## 2. 代码风格 (Code Style)

### 2.1 Kotlin
*   遵循官方 [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)。
*   **命名**:
    *   Class/Interface: `PascalCase`
    *   Function/Variable: `camelCase`
    *   Constant: `UPPER_SNAKE_CASE`
*   **可见性**: 尽可能缩小可见性范围 (优先使用 `private`, `internal`)。

### 2.2 Jetpack Compose
*   **Composable 函数**: 必须使用 `PascalCase` 命名，且作为名词或名词短语（e.g., `FitnessPlanScreen`, `PrimaryButton`）。
*   **State Hoisting**: 尽可能将状态提升到父组件或 ViewModel，保持 Composable 无状态。
*   **Modifiers**: `modifier` 参数应作为 Composable 的第一个可选参数。

## 3. 架构规范 (Architecture)

### 3.1 MVVM & Clean Architecture
*   **UI Layer**: 仅包含 UI 逻辑。数据必须来自 ViewModel 的 `StateFlow`。
*   **ViewModel**: 不持有 Android Framework 引用（如 Context, View），只暴露状态和处理事件。
*   **Repository**: 数据源的唯一入口。业务逻辑应尽可能下沉到 Repository 或 UseCase。
*   **Data Source**: 
    *   本地数据使用 Room (`Dao`).
    *   远程/系统数据使用 Service/Manager 封装。

### 3.2 依赖注入 (Hilt)
*   所有依赖必须通过构造函数注入 (`@Inject constructor`).
*   使用 `@Singleton` 标注全局单例。
*   ViewModel 使用 `@HiltViewModel` 标注。

## 4. 文档规范 (Documentation)

### 4.1 KDoc
*   **所有 public 类、方法、属性**必须包含 KDoc 注释。
*   注释应包含：
    *   功能简述。
    *   `@param`: 参数说明。
    *   `@return`: 返回值说明。
    *   `@throws`: 可能抛出的异常。

### 4.2 Markdown 文档
*   技术文档更新应与代码变更同步。
*   保持文档目录结构清晰，使用正确的 Markdown 语法。

## 5. 资源文件 (Resources)
*   **Strings**: 所有 UI 显示的文本必须提取到 `strings.xml`，禁止硬编码。
*   **Colors**: 颜色定义在 `Color.kt` 或 `theme/` 下，禁止直接使用 Hex 值。

## 6. AI 助手行为准则 (AI Agent Rules)

### 6.1 角色定位
*   AI 助手作为**高级结对程序员**，应主动思考、发现问题并提出解决方案。
*   不仅仅是执行指令，更要理解背后的业务逻辑和技术背景。

### 6.2 交互模式
*   **主动性**: 在执行任务前，先探索代码库，理解上下文。
*   **完整性**: 确保生成的代码是完整的、可编译的、经过验证的。
*   **透明度**: 清晰地解释修改的原因、影响范围和验证结果。

### 6.3 文档维护
*   **同步更新**: 修改代码时，必须同步更新相关的技术文档（如架构图、API 文档、数据库文档）。
*   **自动检查**: 在完成任务前，检查是否需要更新 README 或 CHANGELOG。

### 6.4 质量保证
*   **自我审查**: 在交付前，自我审查代码是否符合上述代码风格和架构规范。
*   **测试驱动**: 尽可能编写或运行测试用例来验证修改的正确性。
