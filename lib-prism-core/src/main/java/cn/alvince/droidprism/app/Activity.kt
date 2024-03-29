package cn.alvince.droidprism.app

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.collection.SparseArrayCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import cn.alvince.droidprism.EZPrism
import cn.alvince.droidprism.internal.Instrumentation
import cn.alvince.droidprism.internal.getOrPut
import cn.alvince.droidprism.internal.lifecycle.observeWith
import cn.alvince.droidprism.internal.logDIfDebug
import cn.alvince.droidprism.internal.postOnMain
import cn.alvince.droidprism.internal.runOnMain
import cn.alvince.droidprism.log.ExposureStateHelper
import cn.alvince.droidprism.log.impl.LogPageEntryDelegate
import cn.alvince.droidprism.log.page.ILogPage
import cn.alvince.droidprism.log.page.LogPageManager
import cn.alvince.droidprism.log.page.PageNameOf
import cn.alvince.zanpakuto.core.text.orDefault
import java.lang.ref.WeakReference

/**
 * Retrieves [ILogPage] from [Activity]
 *
 * @throws IllegalArgumentException activity destroyed
 * @throws IllegalStateException activity not implements [ILogPage], nor use-raw-page via [EZPrism.useRawPage]
 */
@Throws(IllegalArgumentException::class, IllegalStateException::class)
fun Activity.asLogPage(): ILogPage {
    require(!isFinishing && !isDestroyed) { "Activity $this is already destroyed." }
    if (this is ILogPage) return this
    check(Instrumentation.useRawPage) { "Must enable raw page via EZPrism.useRawPage() first." }
    return obtainLogPage()
}

val Activity.logExposeStateHelper: ExposureStateHelper?
    get() = try {
        asLogPage().exposureStateHelper
    } catch (e: IllegalArgumentException) {
        null
    } catch (e: IllegalStateException) {
        null
    }

private val activityPageCache = SparseArrayCompat<ActivityPageRecord>()

private fun Activity.obtainLogPage(name: String = ""): ILogPage {
    val k = this.hashCode()
    return activityPageCache.getOrPut(k) {
        ActivityPageRecord(k, this, LogPageEntryDelegate().apply { setPageName(PageNameOf(name.orDefault(this@obtainLogPage::class.java.simpleName))) })
    }
        .page
}

private class ActivityPageRecord(private val key: Int, activity: Activity, val page: ILogPage) : Application.ActivityLifecycleCallbacks {

    val target = WeakReference(activity)

    private var _disposed: Boolean = false

    init {
        if (activity is LifecycleOwner) {
            observeComponentActivity(activity)
        } else {
            observeActivityCompat(activity)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity === target.get()) {
            LogPageManager.changePage(page)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity === target.get()) {
            dispatchPageShowChanged(true)
        }
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity === target.get()) {
            dispatchPageShowChanged(false)
        }
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity === target.get()) {
            dispose()
        }
    }

    private fun observeComponentActivity(activity: LifecycleOwner) {
        activity.observeWith { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                runOnMain { dispose() }
                return@observeWith
            }
            if (event >= Lifecycle.Event.ON_PAUSE && event < Lifecycle.Event.ON_DESTROY) {
                dispatchPageShowChanged(false)
                return@observeWith
            }
            if (event >= Lifecycle.Event.ON_START && event < Lifecycle.Event.ON_PAUSE) {
                if (event == Lifecycle.Event.ON_START) {
                    LogPageManager.changePage(page)
                }
                dispatchPageShowChanged(true)
            }
        }
    }

    private fun observeActivityCompat(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.registerActivityLifecycleCallbacks(this)
        } else {
            activity.application.registerActivityLifecycleCallbacks(this)
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
        postOnMain { activityPageCache.remove(key) }
    }
}
