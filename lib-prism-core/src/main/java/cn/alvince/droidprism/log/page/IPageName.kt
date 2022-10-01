package cn.alvince.droidprism.log.page

import cn.alvince.zanpakuto.core.text.orDefault

/**
 * Log page name indicator
 */
interface IPageName {
    val id: String

    val displayName: String get() = id
}

/**
 * Simple [IPageName] impl
 */
class SimplePageName(pageId: String, display: String? = null) : IPageName {
    override val id: String = pageId

    override val displayName: String = display.orDefault(pageId)
}

@Suppress("FunctionName") // Create IPageName via function
fun PageNameOf(pageId: String, displayName: String? = null): IPageName = SimplePageName(pageId, displayName)
