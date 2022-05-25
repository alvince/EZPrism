package cn.alvince.droidprism.log.impl

import cn.alvince.droidprism.log.ExposureStateHelper
import cn.alvince.droidprism.log.ILogPage

class LogPageDelegate : ILogPage {

    override val exposureStateHelper: ExposureStateHelper
        get() {
            return _exposureStateHelper
                ?: ExposureStateHelper().apply {
                    _exposureStateHelper = this
                    pageShowing = showing
                }
        }

    private var _exposureStateHelper: ExposureStateHelper? = null

    private var showing: Boolean = false

    override fun onPageShowingChanged(show: Boolean) {
        showing = show
        exposureStateHelper.pageShowing = show
    }
}
