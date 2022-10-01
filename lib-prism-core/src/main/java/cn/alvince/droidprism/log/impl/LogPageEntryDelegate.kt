package cn.alvince.droidprism.log.impl

import cn.alvince.droidprism.internal.plus
import cn.alvince.droidprism.log.page.EZPageDurationCalc
import cn.alvince.droidprism.log.page.ILogPageEntry
import cn.alvince.droidprism.log.page.IPageDurationCalc
import cn.alvince.droidprism.log.page.IPageName
import cn.alvince.zanpakuto.core.serialization.JSONCreator
import org.json.JSONObject

/**
 * [ILogPageEntry] implementation for delegate page base behaviors
 *
 * Create by bytedance on 2022/9/24
 *
 * @author zhangyang.alvince@bytedance.com
 */
class LogPageEntryDelegate() : LogPageDelegate(""), ILogPageEntry, IPageDurationCalc by EZPageDurationCalc() {

    override val content: JSONObject get() = pageContentData

    override var className: String = ""

    private val pageContentData = JSONObject()


    constructor(id: IPageName) : this() {
        pageId = id
    }

    override fun setPageName(pageName: IPageName) {
        pageId = pageName
    }

    override fun setPageContent(content: JSONObject) {
        pageContentData + content
    }

    override fun setPageContent(content: JSONCreator.() -> Unit) {
        JSONObject()
            .also { JSONCreator(it).apply(content) }
            .also {
                pageContentData + it
            }
    }
}
