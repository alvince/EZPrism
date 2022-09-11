package cn.alvince.droidprism.sample.trace

import cn.alvince.droidprism.log.ActionType
import cn.alvince.droidprism.log.ITraceable
import cn.alvince.zanpakuto.core.serialization.json
import org.json.JSONObject

/**
 * Sample custom trace model
 *
 * Create by bytedance on 2022/9/11
 *
 * @author zhangyang.alvince@bytedance.com
 */
open class SampleTrace(val name: String) : ITraceable {

    override fun toActionJson(actionType: ActionType): JSONObject = json {
        "event_id" to name
    }

    override fun toString(): String {
        return "SampleTrace(name='$name')"
    }
}
