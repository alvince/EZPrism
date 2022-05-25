package cn.alvince.droidprism.log

import androidx.collection.ArraySet
import androidx.collection.arrayMapOf
import cn.alvince.droidprism.internal.Instrumentation
import cn.alvince.droidprism.internal.mainHandler
import cn.alvince.zanpakuto.core.property.ObservableProperty
import cn.alvince.zanpakuto.core.time.Timestamp

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
        val hasChanged = if (showing) {
            traceElements.add(traceItem)
        } else {
            traceElements.remove(traceItem)
        }
        if (hasChanged && pageShowing) {
            val state = getState(traceItem)
            if (state.showing != showing) {
                state.showing = showing
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

    private inner class TraceItemState(val trace: ITraceable) {

        var lastShowTime: Timestamp = Timestamp.ZERO
        var lastHideTime: Timestamp = Timestamp.ZERO

        var showing: Boolean by ObservableProperty(false) { changeShowingState(it) }

        private var scheduleExposeEvent: Runnable? = null

        private var scheduled = false
        private var hasCancelledScheduling = false

        private fun changeShowingState(showing: Boolean) {
            if (showing) {
                lastShowTime = Timestamp.now()
                if (lastShowTime - lastHideTime >= Instrumentation.exposeTimeThreshold || hasCancelledScheduling) {
                    var r = scheduleExposeEvent
                    if (r == null) {
                        r = Runnable {
                            Instrumentation.emitExposeEventIfNotTooFrequent(trace)
                            scheduled = false
                        }.also { scheduleExposeEvent = it }
                    }
                    scheduled = true
                    mainHandler.postDelayed(r, Instrumentation.exposeTimeThreshold.inMillis)
                }
                hasCancelledScheduling = false
            } else {
                lastHideTime = Timestamp.now()
                hasCancelledScheduling = scheduled
                scheduleExposeEvent?.let {
                    mainHandler.removeCallbacks(it)
                    scheduled = false
                }
            }
        }
    }
}
