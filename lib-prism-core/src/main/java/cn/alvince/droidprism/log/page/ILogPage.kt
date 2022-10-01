package cn.alvince.droidprism.log.page

import cn.alvince.droidprism.log.ExposureStateHelper

interface ILogPage {

    fun interface OnPageShowStatusChangeListener {
        fun onPageShowStatusChanged(show: Boolean)
    }

    val exposureStateHelper: ExposureStateHelper

    fun onPageShowingChanged(show: Boolean)

    fun pageName(): IPageName

    fun addOnPageShowStatusChangedListener(listener: OnPageShowStatusChangeListener)
}
