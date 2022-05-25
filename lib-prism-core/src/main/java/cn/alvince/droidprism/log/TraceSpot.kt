package cn.alvince.droidprism.log

import cn.alvince.zanpakuto.core.serialization.json
import org.json.JSONObject

data class TraceSpot internal constructor(
    val traceAction: String,
    val primaryType: String,
    val primaryId: String,
    val extend: Map<String, *>? = null,
    val exposeId: String? = null,
    var serialNo: Int = 0, // prevent hash collision for instances with the same data
) : ITraceable {

    override fun toActionJson(actionType: ActionType): JSONObject = json {
        "event_id" to eventId(actionType)
        "action_primary_id" indeed primaryId
        "action_primary_type" indeed primaryType
        "action_extend" indeed extend?.takeUnless { it.isEmpty() }?.toString()
        "expose_id" indeed exposeId
    }

    override fun toExposeJson(): JSONObject = json {
        "event_id" to eventId(ActionType.EXPOSE)
        "expose_primary_id" indeed primaryId
        "expose_primary_type" indeed primaryType
        "expose_extend" indeed extend?.takeUnless { it.isEmpty() }?.toString()
        "expose_id" indeed exposeId
    }

    private fun eventId(actionType: ActionType): String = "${traceAction}_${actionType.typeName}"

    companion object {
        fun of(action: String) = TraceSpot(action, "", "")

        fun of(action: String, primaryType: String, primaryId: String) = TraceSpot(action, primaryType, primaryId)
    }
}
