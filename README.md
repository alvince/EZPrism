EZPrism
===

EZPrism 是为 `Android` 上运行的应用程序提供数据埋点能力的中间件  
系统基于 `View` 的行为监听，以及抽象的 `Page` 和 `Trace` 构建出的高度可扩展的埋点能力框架

__Snapshot, add config:__
```groovy
repositories {
    maven {
        url 'https://s01.oss.sonatype.org/content/repositories/snapshots/'
    }
}
```

Dependency
```groovy
dependencies {
    implementation 'cn.alvince.droidprism:ezprism:0.0.1-SNAPSHOT'
}
```

使用
---

配置

```kotlin
EZPrism.devMode(true) // 开启调试日志
    .useRawPage() // 使用原生页面（Activity/Fragment) 直接作为日志页面
    .addPrinter(CustomPrismLogcatSink()) // 自定日志输出记录
```

自定义日志数据类

```kotlin
class CustomTrace() : ITraceable {

    fun toActionJson(actionType: ActionType): JSONObject {
        // 根据 action 类型序列化输出
    }

    fun toExposeJson(): JSONObject {
        // 曝光数据序列化输出
    }
}
```

追踪曝光

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        …
        // TraceSpot: built-in traceable data model
        fab.traceExpose(asLogPage(), TraceSpot.of("fab_button"))
        …
    }

    …
}
```

说明
---

主要概念：
- 页面：ILogPage. 埋点事件以页面为单位整合管理，事件行为依赖所属页面的状态
- 事件：ITraceable. 埋点事件的数据载体，设计为接口可自由实现为业务所需的数据结构
