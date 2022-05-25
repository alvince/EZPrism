package cn.alvince.droidprism.app

import androidx.collection.SparseArrayCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import cn.alvince.droidprism.EZPrism
import cn.alvince.droidprism.internal.Instrumentation
import cn.alvince.droidprism.internal.getOrPut
import cn.alvince.droidprism.internal.lifecycle.observeWith
import cn.alvince.droidprism.log.ExposureStateHelper
import cn.alvince.droidprism.log.ILogPage
import cn.alvince.droidprism.log.impl.LogPageDelegate
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

private fun Fragment.obtainLogPage(): ILogPage {
    val k = this.hashCode()
    return fragmentPageCache.getOrPut(k) {
        FragmentPageRecord(k, this, LogPageDelegate())
    }
        .page
}

private class FragmentPageRecord(private val key: Int, fragment: Fragment, val page: ILogPage) {

    val target = WeakReference(fragment)

    private var _disposed: Boolean = false

    init {
        fragment.observeWith { _, event ->
            if (event < Lifecycle.Event.ON_PAUSE) {
                page.onPageShowingChanged(true)
                return@observeWith
            }
            if (event < Lifecycle.Event.ON_DESTROY) {
                page.onPageShowingChanged(false)
                return@observeWith
            }
            if (event == Lifecycle.Event.ON_DESTROY) {
                dispose()
            }
        }
    }

    private fun dispose() {
        if (_disposed) {
            return
        }
        page.onPageShowingChanged(false)
        _disposed = true
        fragmentPageCache.remove(key)
    }
}
