package cn.alvince.droidprism.internal

import cn.alvince.droidprism.log.ActionType
import cn.alvince.droidprism.log.ITraceable

class TraceEmitter {

    fun emitAction(actionType: ActionType, trace: ITraceable) {
        TraceShooter.sendWithCommonFields(actionType, actionType.traceToJson(trace))
    }
}
