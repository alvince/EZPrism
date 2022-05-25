package cn.alvince.droidprism.internal

import cn.alvince.droidprism.log.ActionType
import org.json.JSONObject

internal object TraceShooter {

    fun pageEntry() {
        TODO("sink page entry event trace")
    }

    fun pageExit() {
        TODO("sink page exit event trace")
    }

    fun sendWithCommonFields(type: ActionType, logData: JSONObject) {
        Instrumentation.traverseSink { it.sink(type, logData) }
    }
}
