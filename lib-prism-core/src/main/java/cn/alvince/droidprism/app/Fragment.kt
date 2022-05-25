package cn.alvince.droidprism.app

import androidx.collection.SparseArrayCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import cn.alvince.droidprism.internal.Instrumentation
import cn.alvince.droidprism.internal.getOrPut
import cn.alvince.droidprism.internal.lifecycle.observeWith
import cn.alvince.droidprism.log.ILogPage
import cn.alvince.droidprism.log.impl.LogPageDelegate
import java.lang.ref.WeakReference

fun Fragment.asLogPage(): ILogPage {
    require(lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) { "Fragment $this is already destroyed." }
    if (this is ILogPage) return this
    check(Instrumentation.useRawPage) { "Must enable raw page via EZPrism.useRawPage() first." }
    return obtainLogPage()
}

private val fragmentPageCache = SparseArrayCompat<FragmentPageRecord>()

private fun Fragment.obtainLogPage(): ILogPage {
    val k = this.hashCode()
    return fragmentPageCache.getOrPut(k) {
        FragmentPageRecord(this, LogPageDelegate())
    }
        .page
}

private class FragmentPageRecord(fragment: Fragment, val page: ILogPage) {

    val target = WeakReference(fragment)

    private var _disposed: Boolean = false

    init {
        fragment.observeWith { _, event ->
            if (event < Lifecycle.Event.ON_PAUSE) {
                page.onPageShowingChanged(true)
                return@observeWith
            }
            when (event) {
                Lifecycle.Event.ON_PAUSE -> page.onPageShowingChanged(false)
                Lifecycle.Event.ON_DESTROY -> _disposed = true
            }
        }
    }
}
