package cn.alvince.droidprism.log.impl

import cn.alvince.droidprism.internal.PAGE_UNDEFINED
import cn.alvince.droidprism.log.ExposureStateHelper
import cn.alvince.droidprism.log.page.ILogPage
import cn.alvince.droidprism.log.page.IPageName
import cn.alvince.droidprism.log.page.PageNameOf
import cn.alvince.zanpakuto.core.text.takeIfNotEmpty

/**
 * [ILogPage] implementation for delegate page base behaviors
 *
 * Created by alvince on 2022/5/23
 *
 * @author alvince.zy@gmail.com
 */
open class LogPageDelegate(name: String) : ILogPage {

    override val exposureStateHelper: ExposureStateHelper
        get() {
            return _exposureStateHelper
                ?: ExposureStateHelper().apply {
                    _exposureStateHelper = this
                    pageShowing = _showing
                }
        }

    protected var pageId = name.takeIfNotEmpty()?.let { PageNameOf(name) } ?: PAGE_UNDEFINED

    private val pageShowingChangedListeners
        get() = synchronized(this) {
            _pageShowingChangedListeners ?: arrayListOf<ILogPage.OnPageShowStatusChangeListener>().also { _pageShowingChangedListeners = it }
        }

    private var _exposureStateHelper: ExposureStateHelper? = null
    private var _showing: Boolean = false

    private var _pageShowingChangedListeners: MutableList<ILogPage.OnPageShowStatusChangeListener>? = null

    override fun onPageShowingChanged(show: Boolean) {
        _showing = show
        exposureStateHelper.pageShowing = show
        pageShowingChangedListeners.forEach { it.onPageShowStatusChanged(show) }
    }

    override fun pageName(): IPageName = pageId

    override fun addOnPageShowStatusChangedListener(listener: ILogPage.OnPageShowStatusChangeListener) {
        pageShowingChangedListeners.add(listener)
    }
}
