package cn.alvince.droidprism.internal

import androidx.annotation.MainThread
import cn.alvince.droidprism.log.ActionType
import cn.alvince.droidprism.log.ITraceable

class TraceEmitter {

    @MainThread
    fun emitAction(actionType: ActionType, trace: ITraceable) {
        logDIfDebug { "emit trace: [${actionType.logType()}] $trace" }
        TraceShooter.sendWithCommonFields(actionType, actionType.traceToJson(trace))
    }
}
