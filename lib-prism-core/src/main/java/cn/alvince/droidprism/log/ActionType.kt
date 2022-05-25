package cn.alvince.droidprism.log

import org.json.JSONObject

enum class ActionType(val typeName: String) {

    /**
     * For click event
     */
    CLICK("click"),

    /**
     * For content exposing
     */
    EXPOSE("view") {
        override fun logType(): String = "Expose"

        override fun traceToJson(trace: ITraceable): JSONObject = trace.toExposeJson()
    },

    ;

    internal open fun logType(): String = "ActionEvent"

    internal open fun traceToJson(trace: ITraceable): JSONObject = trace.toActionJson(this)
}
