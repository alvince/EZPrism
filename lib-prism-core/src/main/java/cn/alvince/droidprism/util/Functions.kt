package cn.alvince.droidprism.util

import android.view.View
import cn.alvince.droidprism.operator.ViewExposureHelper
import cn.alvince.droidprism.operator.monitorExposureState

fun ViewExposureHelper.watch(view: View) {
    view.monitorExposureState(this)
}
