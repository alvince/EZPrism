EZPrism
===

EZPrism 是为 `Android` 上运行的应用程序提供埋点数据收集能力的中间件  
系统基于 `View` 的行为监听，以及抽象的 `Page` 和 `Trace` 构建出的可扩展的埋点能力框架

__~~Snapshot, add config:~~__
```groovy
// repositories {
//     maven { url 'https://s01.oss.sonatype.org/content/repositories/snapshots/' }
// }
```

Dependency
```groovy
dependencies {
    implementation 'cn.alvince.droidprism:ezprism:0.0.2'
}
```

Sample
---

运行 app 模块，`Logcat` tag: `EZPrism`

- 查看事件触发日志：```emit trace: [${actionType.logType()}] $trace```
- 埋点数据日志：[CustomPrismLogcatSink](app/src/main/java/cn/alvince/droidprism/sample/log/CustomPrismLogcatSink.kt) ```——> [custom sink] ${type.typeName} - $logData```

使用
---

#### 配置/初始化

添加应用自定埋点记录器 | [sample for print logcat](app/src/main/java/cn/alvince/droidprism/sample/log/CustomPrismLogcatSink.kt)

```kotlin
EZPrism.devMode(true) // 开启调试日志
    .useRawPage() // 使用原生页面（Activity/Fragment) 直接作为逻辑页面
    /* 添加日志输出记录，实现埋点数据的自定义上报逻辑（logcat 日志，文件 I/O, 网络上传，委托三方 SDK 上报等）*/
    .addPrinter(CustomPrismLogcatSink())
```

#### 自定义日志数据类

App 自定义埋点数据类，用于承载埋点数据和，及实现 json 序列化，需实现 `ITraceable` 接口  

```kotlin
class CustomTrace() : ITraceable {

    override fun toActionJson(actionType: ActionType): JSONObject {
        // 根据 action 类型序列化输出
    }

    override fun toExposeJson(): JSONObject {
        // 曝光数据序列化输出
    }
}
```

#### 追踪曝光

`View` 的曝光监听依赖 `ViewTraceHelper` 完成，内部依赖 `ViewExposureHelper` 完成对曝光的侦听  
`ViewExposureHelper` 依赖 `ILogPage` (逻辑页面) 提供的 `exposureStateHelper`：`ExposureStateHelper` 管理曝光状态  
每个 `View` 对象对应一个 `ViewTraceHelper` 实例  
每个 `ILogPage` 对应一个 `ExposureStateHelper` 实例  

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        …
        // TraceSpot: built-in traceable data model
        // 快速开启 `View` 的曝光监听：fun View.traceExpose(ILogPage, ITraceable) 扩展的增强函数
        // 效果：fab 按钮每次从不可见变成可见时，出发埋点，json: {"event_id": "fab_button_expose"}
        fab.traceExpose(asLogPage(), TraceSpot.of("fab_button"))
        …
    }

    …
}
```

#### 使用方法

- 获取 `ILogPage`
    ```kotlin
    // 1. 原生页面直接获取，require: EZPrism.useRawPage()
    val exposureStateHelper = activity.asLogPage().exposureStateHelper
    val exposureStateHelper = fragment.asLogPage().exposureStateHelper

    // 2. 实现 ILogPage 接口，比如在自定义 View、Dialog 等（如需作为独立的曝光页面）
    ```
- 获取 `ViewTraceHelper`
    ```kotlin
    val traceHelper = view.getTraceHelper()
    ```
- 激活
    ```kotlin
    traceHelper.apply {
        trace = … // 赋值 ITraceable，绑定埋点数据对象
        exposureStateHelper = page.exposureStateHelper // 绑定曝光状态辅助器，可从 ILogPage 获取
    }
    ```

说明
---

主要概念：
- 页面：`ILogPage`. 埋点事件以页面为单位整合管理，事件行为依赖所属页面的状态
- 事件：`ITraceable`. 埋点事件的数据载体，设计为接口可自由实现为业务所需的数据结构
- 埋点跟踪辅助器：`ViewTraceHelper`. 视图 `View` 埋点曝光组件，与 `View` 一一对应
- 曝光状态辅助器：`ExposureStateHelper`. 数据曝光状态管理辅助器，与 `ILogPage` 一一对应

Changes:
---

### 0.0.2

feature:

- 增加新的曝光模式：视图不可见时触发
  ```kotlin
  class CustomTrace : ITraceable, ITraceWhenInvisibleWithDuration by SimpleDuration() {
      …
  }
  ```
  携带一次从可见到不可见的曝光时长 `duration`
- 优化：删除了冗余的组件: `ViewExposureHelper`，直接又 `ViewTraceHelper` 控制曝光和埋点数据
