package cn.alvince.droidprism.internal

import cn.alvince.droidprism.IPrismSink
import cn.alvince.droidprism.log.ActionType
import cn.alvince.droidprism.log.ITraceable
import cn.alvince.zanpakuto.core.time.Duration
import java.util.LinkedList
import java.util.concurrent.TimeUnit

/**
 * Internal prism instrumentation
 *
 * Created by alvince on 2022/5/23
 *
 * @author alvince.zy@gmail.com
 */
internal object Instrumentation {

    internal val eventEmitter = TraceEmitter()

    internal val exposeTimeThreshold: Duration get() = customExposeTimeThreshold.takeOr { defaultExposeTimeThreshold }
    internal val exposeFrequencyThreshold: Duration get() = customExposeFrequencyThreshold.takeOr { defaultExposeFrequencyThreshold }

    internal var devMode: Boolean = false

    @Volatile
    internal var useRawPage: Boolean = false

    private val defaultExposeTimeThreshold = Duration.of(500L, TimeUnit.MILLISECONDS)
    private val defaultExposeFrequencyThreshold = Duration.of(10, TimeUnit.SECONDS)
    private val sinks = LinkedList<IPrismSink>()

    private var customExposeTimeThreshold = Duration.ZERO
    private var customExposeFrequencyThreshold = Duration.ZERO

    @Synchronized
    fun addSink(sink: IPrismSink) {
        sinks.takeIf { !it.contains(sink) }?.add(sink)
    }

    fun traverseSink(block: (IPrismSink) -> Unit) {
        sinks.toList().forEach(block)
    }

    fun setCustomExposeTimeThreshold(duration: Duration) {
        customExposeTimeThreshold = duration
    }

    fun setCustomExposeFrequencyThreshold(internal: Duration) {
        customExposeFrequencyThreshold = internal
    }

    internal fun emitExposeEventIfNotTooFrequent(trace: ITraceable) {
        trace.takeIf { ITraceable.checkExposeNotTooFrequent(it) }
            ?.also {
                runOnMain { eventEmitter.emitAction(ActionType.EXPOSE, trace) }
            }
    }

    private inline fun Duration.takeOr(defaultVal: () -> Duration) = this.takeUnless { it == Duration.ZERO } ?: defaultVal()
}
