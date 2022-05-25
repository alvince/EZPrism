package cn.alvince.droidprism.app

import android.app.Activity
import androidx.collection.SparseArrayCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import cn.alvince.droidprism.internal.Instrumentation
import cn.alvince.droidprism.internal.getOrPut
import cn.alvince.droidprism.internal.lifecycle.observeWith
import cn.alvince.droidprism.log.ILogPage
import cn.alvince.droidprism.log.impl.LogPageDelegate
import java.lang.ref.WeakReference

fun Activity.asLogPage(): ILogPage {
    require(!isFinishing && !isDestroyed) { "Activity $this is already destroyed." }
    if (this is ILogPage) return this
    check(Instrumentation.useRawPage) { "Must enable raw page via EZPrism.useRawPage() first." }
    return obtainLogPage()
}

private val activityPageCache = SparseArrayCompat<ActivityPageRecord>()

private fun Activity.obtainLogPage(): ILogPage {
    val k = this.hashCode()
    return activityPageCache.getOrPut(k) {
        ActivityPageRecord(this, LogPageDelegate())
    }
        .page
}

private class ActivityPageRecord(activity: Activity, val page: ILogPage) {

    val target = WeakReference(activity)

    private var _disposed: Boolean = false

    init {
        if (activity is LifecycleOwner) {
            observeComponentActivity(activity)
        } else {
            observeActivityCompat(activity)
        }
    }

    private fun observeComponentActivity(activity: LifecycleOwner) {
        activity.observeWith { _, event ->
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

    private fun observeActivityCompat(activity: Activity) {

    }
}
