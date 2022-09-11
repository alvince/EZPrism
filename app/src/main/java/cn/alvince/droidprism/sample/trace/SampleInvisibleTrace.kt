package cn.alvince.droidprism.sample.trace

import cn.alvince.droidprism.log.ActionType
import cn.alvince.droidprism.log.ITraceWhenInvisibleWithDuration
import cn.alvince.droidprism.log.SimpleDuration
import cn.alvince.zanpakuto.core.serialization.json
import org.json.JSONObject

/**
 * Sample custom trace model that track when invisible only, with exposure duration report
 *
 * Create by bytedance on 2022/9/11
 *
 * @author zhangyang.alvince@bytedance.com
 */
class SampleInvisibleTrace(name: String) : SampleTrace(name), ITraceWhenInvisibleWithDuration by SimpleDuration() {

    override fun toActionJson(actionType: ActionType): JSONObject = json {
        "event_id" to name
        if (actionType == ActionType.EXPOSE) {
            "duration" to duration.inSeconds
        }
    }

    override fun toString(): String {
        return "SampleInvisibleTrace(name='$name', duration='$duration')"
    }
}
