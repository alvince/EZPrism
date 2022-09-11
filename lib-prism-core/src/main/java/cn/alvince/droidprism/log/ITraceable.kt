package cn.alvince.droidprism.log

import cn.alvince.droidprism.internal.Instrumentation
import cn.alvince.zanpakuto.core.time.Duration
import cn.alvince.zanpakuto.core.time.Timestamp
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Traceable data defined
 *
 * Created by alvince on 2022/5/23
 *
 * @author alvince.zy@gmail.com
 */
interface ITraceable {

    fun toActionJson(actionType: ActionType): JSONObject

    fun toExposeJson(): JSONObject = toActionJson(ActionType.EXPOSE)

    companion object {
        private val exposeMonitorMap = mutableMapOf<ITraceable, Timestamp>()
        private val exposeTimeCleanupThreshold = Duration.of(1, TimeUnit.MINUTES)
        private var lastCleanupTime = Timestamp.ZERO

        internal fun checkExposeNotTooFrequent(trace: ITraceable): Boolean {
            val now = Timestamp.now()
            val frequencyLimit = Instrumentation.exposeFrequencyThreshold
            if (now - lastCleanupTime > exposeTimeCleanupThreshold) {
                var cleared = false
                exposeMonitorMap.iterator().also { iterator ->
                    while (iterator.hasNext()) {
                        val (_, time) = iterator.next()
                        if (now - time > frequencyLimit) {
                            iterator.remove()
                            cleared = cleared || true
                        }
                    }
                }
                if (cleared) lastCleanupTime = now
            }
            if (exposeMonitorMap[trace].let { it != null && now - it < frequencyLimit }) {
                return false
            }
            exposeMonitorMap[trace] = now
            return true
        }
    }
}

/**
 * Indicate that should trace while target invisible to users
 */
interface ITraceWhenInvisible

/**
 * Trace while target invisible, with exposed duration
 */
interface ITraceWhenInvisibleWithDuration : ITraceWhenInvisible {
    var duration: Duration
}

/**
 * Simple holder proxy of [ITraceWhenInvisibleWithDuration]
 */
class SimpleDuration : ITraceWhenInvisibleWithDuration {
    override var duration: Duration = Duration.ZERO
}
