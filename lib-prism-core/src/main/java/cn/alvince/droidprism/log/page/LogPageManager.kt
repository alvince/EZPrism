package cn.alvince.droidprism.log.page

import android.util.Log
import androidx.collection.SparseArrayCompat
import cn.alvince.droidprism.internal.LOG_TAG
import cn.alvince.droidprism.internal.PAGE_EMPTY
import cn.alvince.droidprism.internal.TraceShooter
import cn.alvince.droidprism.internal.checkMainThread
import cn.alvince.droidprism.log.impl.LogPageEntryDelegate
import java.lang.ref.WeakReference

/**
 * Log page state and entry manager
 *
 * Create by bytedance on 2022/9/24
 *
 * @author zhangyang.alvince@bytedance.com
 */
object LogPageManager {

    /**
     * virtual page node that indicate current background
     */
    private val cEmptyPageHolder = LogPageEntryDelegate(PAGE_EMPTY)
    private val rootEntry = PageEntryNode(0, cEmptyPageHolder, null, false)

    private var curPageEntry: ILogPageEntry? = null

    private var pageNode = rootEntry
    private var pageEntryIndex = SparseArrayCompat<PageEntryNode>()

    fun changePage(page: ILogPage) {
        checkMainThread()
        if (page is ILogPageEntry) {
            setCurrentEntry(page)
        } else {
            Log.e(LOG_TAG, "Can not recognize $page")
        }
    }

    private fun setCurrentEntry(pageEntry: ILogPageEntry) {
        if (pageNode.target == pageEntry) {
            return
        }
        setCurrentEntryImpl(pageEntry)
    }

    private fun setCurrentEntryImpl(pageEntry: ILogPageEntry) {
        val curNode = pageNode.checkAvailableEntry()
        val hash = pageEntry.hashCode()
        pageNode += PageEntryNode(hash, pageEntry, curNode)
//        val curEmpty = curPageEntry == null
        curPageEntry?.also { TraceShooter.pageExit(it) }
        curPageEntry = pageEntry
        pageEntryIndex.putIfAbsent(hash, pageNode)
        TraceShooter.pageEntry(pageEntry)
    }

    private fun PageEntryNode.checkAvailableEntry(): PageEntryNode {
        if (prev == null) return this // current is root

        var node = this
        var p: PageEntryNode
        while (node.pageEntry.get() == null) {
            p = node.requirePrev()
            node.discard()
            node = p
        }
        return node
    }

    private inline fun PageEntryNode.discard() {
        prev?.next = null
        prev = null
        pageEntryIndex.remove(id)
    }

    /**
     * Page node record
     *
     * record page entry with a list chain
     *
     * --------    -------------         ----------------
     * | root | -> | next node | -> … -> | current node |
     * --------    -------------         ----------------
     */
    private class PageEntryNode constructor(val id: Int, page: ILogPageEntry, var prev: PageEntryNode?, requirePrev: Boolean = true) {

        val target: ILogPageEntry? get() = pageEntry.get()

        var pageEntry: WeakReference<ILogPageEntry> = WeakReference(page)

        var next: PageEntryNode? = null

        init {
            if (requirePrev) {
                requireNotNull(prev) { "Require prev entry node" }
            }
        }

        operator fun plus (node: PageEntryNode): PageEntryNode {
            return node.also {
                this.next = it
                it.prev = this
            }
        }

        fun requirePrev(): PageEntryNode = checkNotNull(prev) { "Invalid error" }
    }
}
