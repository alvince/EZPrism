package cn.alvince.droidprism

import cn.alvince.droidprism.internal.Instrumentation
import cn.alvince.zanpakuto.core.time.Duration
import java.util.concurrent.TimeUnit

object EZPrism {

    /**
     * Enable or disable developer mode (for print logs), `false` default
     */
    fun devMode(enable: Boolean) {
        Instrumentation.devMode = enable
    }

    fun useRawPage() {
        Instrumentation.useRawPage = true
    }

    /**
     * Add trace
     */
    fun addPrinter(sink: IPrismSink) {
        Instrumentation.addSink(sink)
    }

    /**
     * Set custom time threshold in milliseconds for view expose detect
     */
    fun setCustomExposeTimeThreshold(timeInMillis: Long) {
        Instrumentation.setCustomExposeTimeThreshold(Duration.of(timeInMillis, TimeUnit.MILLISECONDS))
    }
}
