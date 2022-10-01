package cn.alvince.droidprism.internal

import cn.alvince.droidprism.log.ActionType
import cn.alvince.droidprism.log.page.ILogPageEntry
import org.json.JSONObject

internal object TraceShooter {

    fun pageEntry(pageEntry: ILogPageEntry) {
//        Instrumentation.traverseSink { it.sink(ActionType.PAGE_ENTER, ) }
    }

    fun pageExit(pageEntry: ILogPageEntry) {
        Instrumentation.traverseSink { it.sink(ActionType.PAGE_EXIT, pageEntry.content) }
    }

    fun sendWithCommonFields(type: ActionType, logData: JSONObject) {
        Instrumentation.traverseSink { it.sink(type, logData) }
    }
}
