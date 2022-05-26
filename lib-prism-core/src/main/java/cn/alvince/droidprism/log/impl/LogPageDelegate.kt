package cn.alvince.droidprism.log.impl

import cn.alvince.droidprism.internal.PAGE_UNDEFINED
import cn.alvince.droidprism.log.ExposureStateHelper
import cn.alvince.droidprism.log.ILogPage
import cn.alvince.droidprism.log.IPageName
import cn.alvince.droidprism.log.PageNameOf
import cn.alvince.zanpakuto.core.text.takeIfNotEmpty

/**
 * [ILogPage] implementation for delegate page base behaviors
 *
 * Created by alvince on 2022/5/23
 *
 * @author alvince.zy@gmail.com
 */
class LogPageDelegate(name: String) : ILogPage {

    override val exposureStateHelper: ExposureStateHelper
        get() {
            return _exposureStateHelper
                ?: ExposureStateHelper().apply {
                    _exposureStateHelper = this
                    pageShowing = _showing
                }
        }

    private val pageName = name.takeIfNotEmpty()?.let { PageNameOf(name) } ?: PAGE_UNDEFINED

    private var _exposureStateHelper: ExposureStateHelper? = null
    private var _showing: Boolean = false

    override fun onPageShowingChanged(show: Boolean) {
        _showing = show
        exposureStateHelper.pageShowing = show
    }

    override fun pageName(): IPageName = pageName
}
