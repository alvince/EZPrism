package cn.alvince.droidprism

import androidx.annotation.MainThread
import cn.alvince.droidprism.log.ActionType
import org.json.JSONObject

interface IPrismSink {

    @MainThread
    fun sink(type: ActionType, logData: JSONObject)
}
