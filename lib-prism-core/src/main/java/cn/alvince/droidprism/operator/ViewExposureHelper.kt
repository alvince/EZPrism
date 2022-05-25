package cn.alvince.droidprism.operator

import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import cn.alvince.droidprism.R
import cn.alvince.droidprism.log.ExposureStateHelper
import cn.alvince.droidprism.log.ITraceable
import java.lang.ref.WeakReference

class ViewExposureHelper(val stateHelper: ExposureStateHelper) {

    var trace: ITraceable? = null
        set(value) {
            val original = field
            if (original != value) {
                original?.also { stateHelper.markExposeState(it, false) }
                field = value
                forceCheck()
            }
        }

    internal var view: WeakReference<View>? = null

    fun forceCheck() {
        view?.get()?.also { onViewLayoutChange(it, this) }
    }
}

fun ExposureStateHelper.createViewExposureHelper() = ViewExposureHelper(this)

internal class ExposeListener(v: View, private val exposureHelper: ViewExposureHelper) : ViewTreeObserver.OnGlobalLayoutListener,
    ViewTreeObserver.OnScrollChangedListener, View.OnAttachStateChangeListener {

    private val viewRef = WeakReference(v)

    override fun onGlobalLayout() {
        viewRef.get()?.also { onViewLayoutChange(it, exposureHelper) }
    }

    override fun onScrollChanged() {
        viewRef.get()?.also { onViewLayoutChange(it, exposureHelper) }
    }

    override fun onViewAttachedToWindow(v: View) {
        v.viewTreeObserver.addOnScrollChangedListener(this)
        v.viewTreeObserver.addOnGlobalLayoutListener(this)
        onViewLayoutChange(v, exposureHelper, true)
    }

    override fun onViewDetachedFromWindow(v: View) {
        v.viewTreeObserver.removeOnScrollChangedListener(this)
        v.viewTreeObserver.removeOnGlobalLayoutListener(this)
        onViewLayoutChange(v, exposureHelper, false)
    }
}

internal fun View.monitorExposureState(viewExposureHelper: ViewExposureHelper?) {
    val original = this.getTag(R.id.view_exposure_listener_holder) as? ExposeListener
    if (original != null) {
        if (this.isAttachedToWindow) {
            original.onViewDetachedFromWindow(this)
        }
        this.removeOnAttachStateChangeListener(original)
    }
    if (viewExposureHelper == null) {
        return
    }
    viewExposureHelper.view = WeakReference(this)
    onViewLayoutChange(this, viewExposureHelper)
    val listener = ExposeListener(this, viewExposureHelper)
    if (this.isAttachedToWindow) {
        listener.onViewAttachedToWindow(this)
    }
    this.addOnAttachStateChangeListener(listener)
    this.setTag(R.id.view_exposure_listener_holder, listener)
}

private fun onViewLayoutChange(v: View, viewExposureHelper: ViewExposureHelper, attachState: Boolean? = null) {
    val traceItem = viewExposureHelper.trace ?: return
    val exposing = isViewExposing(v, attachState)
    viewExposureHelper.stateHelper.markExposeState(traceItem, exposing)
}

private fun isViewExposing(v: View, attachState: Boolean?): Boolean {
    if (!actualVisible(v)) {
        return false
    }
    if (actualAlpha(v) < 0.01) {
        return false
    }
    if (!(attachState ?: v.isAttachedToWindow)) {
        return false
    }
    val w = v.width
    val h = v.height
    if (w <= 0 || h <= 0) return false
    val out = intArrayOf(0, 0)
    v.getLocationOnScreen(out)
    val x = out[0] + w / 2
    val y = out[1] + h / 2
    return checkPointShowing(x, y, v, out)
}

private fun actualAlpha(v: View): Float {
    var r = 1.0F
    v.forEachAncestor { r *= it.alpha }
    return r
}

private fun actualVisible(v: View): Boolean {
    v.forEachAncestor {
        if (!v.isVisible) {
            return false
        }
    }
    return true
}

private fun checkPointShowing(x: Int, y: Int, v: View, out: IntArray): Boolean {
    v.forEachAncestor { view ->
        view.getLocationOnScreen(out)
        val vx = out[0]
        val vy = out[1]
        if (x !in vx..vx + view.width || y !in vy..vy + view.height) {
            return false
        }
    }
    return true
}

private inline fun View.forEachAncestor(withThis: Boolean = true, block: (View) -> Unit) {
    var view: View? = this
    while (view != null) {
        if (withThis || view !== this) {
            block(view)
        }
        view = view.parent as? View
    }
}
