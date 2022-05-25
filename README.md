EZPrism
===

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

接入使用
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
