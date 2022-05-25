package cn.alvince.droidprism.internal

import cn.alvince.droidprism.IPrismSink
import cn.alvince.droidprism.log.ActionType
import cn.alvince.droidprism.log.ITraceable
import cn.alvince.zanpakuto.core.time.Duration
import java.util.LinkedList
import java.util.concurrent.TimeUnit

internal object Instrumentation {

    internal val exposeTimeThreshold: Duration get() = if (customExposeTimeThreshold != Duration.ZERO) defaultExposeTimeThreshold else customExposeTimeThreshold
    internal val eventEmitter = TraceEmitter()

    internal var devMode: Boolean = false

    @Volatile
    internal var useRawPage: Boolean = false

    private val defaultExposeTimeThreshold = Duration.of(500L, TimeUnit.MILLISECONDS)
    private val sinks = LinkedList<IPrismSink>()

    private var customExposeTimeThreshold = Duration.ZERO

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

    internal fun emitExposeEventIfNotTooFrequent(trace: ITraceable) {
        trace.takeIf { ITraceable.checkExposeNotTooFrequent(it) }
            ?.also {
                runOnMain { eventEmitter.emitAction(ActionType.EXPOSE, trace) }
            }
    }
}
