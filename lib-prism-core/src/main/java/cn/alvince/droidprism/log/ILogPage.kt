package cn.alvince.droidprism.log

interface ILogPage {
    val exposureStateHelper: ExposureStateHelper

    fun onPageShowingChanged(show: Boolean)
}
