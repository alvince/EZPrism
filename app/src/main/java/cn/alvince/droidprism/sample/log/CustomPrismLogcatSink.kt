package cn.alvince.droidprism.sample.log

import android.util.Log
import cn.alvince.droidprism.EZPrism
import cn.alvince.droidprism.IPrismSink
import cn.alvince.droidprism.log.ActionType
import org.json.JSONObject

class CustomPrismLogcatSink : IPrismSink {

    override fun sink(type: ActionType, logData: JSONObject) {
        Log.d("EZPrism", "——> [custom sink] ${type.typeName} - $logData")
    }

    companion object {
        private var prepared = false

        fun prepare() {
            if (prepared) {
                return
            }
            EZPrism.devMode(true)
                .useRawPage()
                .addPrinter(CustomPrismLogcatSink())
            prepared = true
        }
    }
}
