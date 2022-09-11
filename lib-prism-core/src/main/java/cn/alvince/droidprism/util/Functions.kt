package cn.alvince.droidprism.util

import android.view.View
import cn.alvince.droidprism.log.ExposureStateHelper
import cn.alvince.droidprism.log.ILogPage
import cn.alvince.droidprism.log.ITraceable
import cn.alvince.droidprism.operator.ViewTraceHelper

fun View.traceExpose(page: ILogPage, trace: ITraceable?) {
    getTraceHelper().apply {
        this.trace = trace
        this.exposureStateHelper = page.exposureStateHelper
    }
}

fun View.exposeWith(exposureStateHelper: ExposureStateHelper, trace: ITraceable?) {
    getTraceHelper().apply {
        this.trace = trace
        this.exposureStateHelper = exposureStateHelper
    }
}

fun View.getTraceHelper(): ViewTraceHelper {
    return ViewTraceHelper.from(this) ?: ViewTraceHelper.create().also { it.attachToView(this) }
}
