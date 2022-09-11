package cn.alvince.droidprism.log

import androidx.collection.ArraySet
import androidx.collection.arrayMapOf
import cn.alvince.droidprism.internal.Instrumentation
import cn.alvince.droidprism.internal.logDIfDebug
import cn.alvince.droidprism.internal.mainHandler
import cn.alvince.zanpakuto.core.property.ObservableProperty
import cn.alvince.zanpakuto.core.time.Duration
import cn.alvince.zanpakuto.core.time.Timestamp

/**
 * [ITraceable] exposure state handler
 *
 * Created by alvince on 2022/5/23
 *
 * @author alvince.zy@gmail.com
 */
class ExposureStateHelper internal constructor() {

    var pageShowing: Boolean by ObservableProperty(false) { show ->
        if (show) {
            traceElements.forEach { getState(it).showing = true }
        } else {
            traceElements.forEach { getState(it).showing = false }
        }
    }

    private val traceElements = ArraySet<ITraceable>()
    private val stateMap = arrayMapOf<ITraceable, TraceItemState>()

    fun markExposeState(traceItem: ITraceable, showing: Boolean) {
        logDIfDebug { "mark expose state: $showing - $traceItem" }
        val hasChanged = if (showing) traceElements.add(traceItem) else traceElements.remove(traceItem)
        if (hasChanged && pageShowing) {
            getState(traceItem).also { state ->
                if (state.showing != showing) {
                    state.showing = showing
                }
            }
        }
    }

    private fun getState(spot: ITraceable): TraceItemState = stateMap.getOrPut(spot) { gc(); TraceItemState(spot) }

    private fun gc() {
        if (stateMap.size > 1000) {
            val now = Timestamp.now()
            stateMap.entries.removeAll { (_, state) -> !state.showing && now - state.lastHideTime > Instrumentation.exposeTimeThreshold }
        }
    }

    private class TraceItemState(val trace: ITraceable) : Runnable {

        var lastShowTime: Timestamp = Timestamp.ZERO
        var lastHideTime: Timestamp = Timestamp.ZERO

        var showing: Boolean by ObservableProperty(false) { changeShowingState(it) }

        private val traceWhenInvisible: Boolean = trace is ITraceWhenInvisible

        private var scheduled = false
        private var hasCancelledScheduling = false

        private var exposureDuration: Duration = Duration.ZERO

        override fun run() {
            logDIfDebug { "request emit expose event: $trace" }
            trace.also {
                beforeTryEmitEvent(it)
                Instrumentation.emitExposeEventIfNotTooFrequent(it)
                (it as? ITraceWhenInvisibleWithDuration)?.duration = Duration.ZERO
            }
            scheduled = false
        }

        private fun beforeTryEmitEvent(trace: ITraceable) {
            (trace as? ITraceWhenInvisibleWithDuration)?.duration = exposureDuration
        }

        private fun changeShowingState(showing: Boolean) {
            logDIfDebug { "change trace state: $showing - $trace" }
            val now = Timestamp.now()
            if (showing) {
                lastShowTime = now
            } else {
                lastHideTime = now
            }
            if (traceWhenInvisible) {
                checkStateOrEmitWhenInvisible(showing, now)
            } else {
                checkStateOrEmit(showing, now)
            }
        }

        private fun clearScheduler() {
            hasCancelledScheduling = scheduled
            mainHandler.removeCallbacks(this)
            scheduled = false
            exposureDuration = Duration.ZERO
            (trace as? ITraceWhenInvisibleWithDuration)?.duration = Duration.ZERO
        }

        private fun checkStateOrEmit(showing: Boolean, now: Timestamp) {
            if (!showing) {
                clearScheduler()
                return
            }
            val threshold = Instrumentation.exposeTimeThreshold
            if (now - lastHideTime >= threshold || hasCancelledScheduling) {
                logDIfDebug { "schedule delayed expose event: $trace" }
                scheduled = true
                mainHandler.postDelayed(this, threshold.inMillis)
            }
            hasCancelledScheduling = false
        }

        private fun checkStateOrEmitWhenInvisible(showing: Boolean, now: Timestamp) {
            if (showing) {
                clearScheduler()
                return
            }
            val threshold = Instrumentation.exposeTimeThreshold
            val interval = now - lastShowTime
            // require the target has been shown once
            if (lastShowTime > Timestamp.ZERO && (interval >= threshold || hasCancelledScheduling)) {
                logDIfDebug { "schedule delayed expose event: $trace" }
                scheduled = true
                exposureDuration = interval
                mainHandler.postDelayed(this, threshold.inMillis)
            }
            hasCancelledScheduling = false
        }
    }
}
