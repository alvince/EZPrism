package cn.alvince.droidprism.log.page

import cn.alvince.zanpakuto.core.serialization.JSONCreator
import org.json.JSONObject

/**
 * [ILogPage] entry that traceable powered by [LogPageManager]
 *
 * Create by bytedance on 2022/9/24
 *
 * @author zhangyang.alvince@bytedance.com
 */
interface ILogPageEntry : ILogPage {

    val content: JSONObject

    var className: String

    fun setPageName(pageName: IPageName)

    fun setPageContent(content: JSONObject)

    fun setPageContent(content: JSONCreator.() -> Unit)
}
