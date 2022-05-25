package cn.alvince.droidprism.log

import cn.alvince.zanpakuto.core.time.Duration
import cn.alvince.zanpakuto.core.time.Timestamp
import org.json.JSONObject
import java.util.concurrent.TimeUnit

interface ITraceable {

    fun toActionJson(actionType: ActionType): JSONObject

    fun toExposeJson(): JSONObject

    companion object {
        private val exposeMonitorMap = mutableMapOf<ITraceable, Timestamp>()
        private val exposeTimeThreshold = Duration.of(10, TimeUnit.SECONDS)
        private val exposeTimeCleanupThreshold = Duration.of(1, TimeUnit.MINUTES)
        private var lastCleanupTime = Timestamp.ZERO

        internal fun checkExposeNotTooFrequent(trace: ITraceable): Boolean {
            val now = Timestamp.now()
            if (now - lastCleanupTime > exposeTimeCleanupThreshold) {
                var cleared = false
                exposeMonitorMap.iterator().also { iterator ->
                    while (iterator.hasNext()) {
                        val (_, time) = iterator.next()
                        if (now - time > exposeTimeThreshold) {
                            iterator.remove()
                            cleared = cleared || true
                        }
                    }
                }
                if (cleared) lastCleanupTime = now
            }
            if (exposeMonitorMap[trace].let { it != null && now - it < exposeTimeThreshold }) {
                return false
            }
            exposeMonitorMap[trace] = now
            return true
        }
    }
}
