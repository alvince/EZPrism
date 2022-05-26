package cn.alvince.droidprism

import cn.alvince.droidprism.internal.Instrumentation
import cn.alvince.zanpakuto.core.time.Duration
import java.util.concurrent.TimeUnit

/**
 * EZPrism access entrance
 *
 * Created by alvince on 2022/5/23
 *
 * @author alvince.zy@gmail.com
 * @see [EZPrism](https://github.com/alvince/EZPrism)
 */
object EZPrism {

    /**
     * Enable or disable developer mode (for print logs), `false` default
     */
    fun devMode(enable: Boolean): EZPrism = this.apply { Instrumentation.devMode = enable }

    fun useRawPage(): EZPrism = this.apply { Instrumentation.useRawPage = true }

    /**
     * Add trace
     */
    fun addPrinter(sink: IPrismSink): EZPrism = this.apply { Instrumentation.addSink(sink) }

    /**
     * Set custom time threshold in milliseconds for view expose detect
     */
    fun setExposeTimeThreshold(timeInMillis: Long): EZPrism = this.apply {
        require(timeInMillis > 0) { "A threshold greater than 0 ms should be set." }
        Instrumentation.setCustomExposeTimeThreshold(Duration.of(timeInMillis, TimeUnit.MILLISECONDS))
    }

    /**
     * Set custom time threshold in milliseconds for exposure data frequency
     */
    fun setExposeFrequencyThreshold(timeInMillis: Long): EZPrism = this.apply {
        require(timeInMillis > 0) { "A threshold greater than 0 ms should be set." }
        Instrumentation.setCustomExposeFrequencyThreshold(Duration.of(timeInMillis, TimeUnit.MILLISECONDS))
    }
}
