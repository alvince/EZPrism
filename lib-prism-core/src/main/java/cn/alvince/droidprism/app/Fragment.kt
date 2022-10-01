package cn.alvince.droidprism.app

import androidx.collection.SparseArrayCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import cn.alvince.droidprism.EZPrism
import cn.alvince.droidprism.internal.Instrumentation
import cn.alvince.droidprism.internal.getOrPut
import cn.alvince.droidprism.internal.lifecycle.observeWith
import cn.alvince.droidprism.internal.logDIfDebug
import cn.alvince.droidprism.internal.postOnMain
import cn.alvince.droidprism.log.ExposureStateHelper
import cn.alvince.droidprism.log.impl.LogPageEntryDelegate
import cn.alvince.droidprism.log.page.ILogPage
import cn.alvince.droidprism.log.page.LogPageManager
import cn.alvince.droidprism.log.page.PageNameOf
import cn.alvince.zanpakuto.core.text.orDefault
import java.lang.ref.WeakReference

/**
 * Retrieves [ILogPage] from [Fragment]
 *
 * @throws IllegalArgumentException fragment destroyed
 * @throws IllegalStateException fragment not implements [ILogPage], nor use-raw-page via [EZPrism.useRawPage]
 */
@Throws(IllegalArgumentException::class, IllegalStateException::class)
fun Fragment.asLogPage(): ILogPage {
    require(lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) { "Fragment $this is already destroyed." }
    if (this is ILogPage) return this
    check(Instrumentation.useRawPage) { "Must enable raw page via EZPrism.useRawPage() first." }
    return obtainLogPage()
}

val Fragment.logExposeStateHelper: ExposureStateHelper?
    get() = try {
        asLogPage().exposureStateHelper
    } catch (e: IllegalArgumentException) {
        null
    } catch (e: IllegalStateException) {
        null
    }

private val fragmentPageCache = SparseArrayCompat<FragmentPageRecord>()

private fun Fragment.obtainLogPage(name: String = ""): ILogPage {
    val k = this.hashCode()
    return fragmentPageCache.getOrPut(k) {
        FragmentPageRecord(k, this, LogPageEntryDelegate().apply { setPageName(PageNameOf(name.orDefault(this@obtainLogPage::class.java.simpleName))) })
    }
        .page
}

private class FragmentPageRecord(private val key: Int, fragment: Fragment, val page: ILogPage) {

    val target = WeakReference(fragment)

    private var _disposed: Boolean = false

    init {
        fragment.observeWith { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                dispose()
                return@observeWith
            }
            if (event >= Lifecycle.Event.ON_PAUSE && event < Lifecycle.Event.ON_DESTROY) {
                dispatchPageShowChanged(false)
                return@observeWith
            }
            if (event > Lifecycle.Event.ON_CREATE && event < Lifecycle.Event.ON_PAUSE) {
                if (event == Lifecycle.Event.ON_RESUME) {
                    LogPageManager.changePage(page)
                }
                dispatchPageShowChanged(true)
            }
        }
    }

    private fun dispatchPageShowChanged(show: Boolean) {
        logDIfDebug { "dispatch page show: [$show] ${page.pageName().id} : ${target.get()}" }
        page.onPageShowingChanged(show)
    }

    private fun dispose() {
        if (_disposed) {
            return
        }
        logDIfDebug { "dispose page: ${page.pageName().id}, ${target.get()}" }
        page.onPageShowingChanged(false)
        _disposed = true
        postOnMain { fragmentPageCache.remove(key) }
    }
}
