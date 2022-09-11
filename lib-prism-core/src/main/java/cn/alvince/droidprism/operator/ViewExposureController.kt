package cn.alvince.droidprism.operator

import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import cn.alvince.droidprism.R
import cn.alvince.droidprism.log.ExposureStateHelper
import cn.alvince.droidprism.log.ITraceable
import java.lang.ref.WeakReference

internal interface IViewExposureController {

    val traceable: ITraceable?

    val stateManager: ExposureStateHelper?
}

internal class ExposeListener(v: View, private val exposureHelper: IViewExposureController) : ViewTreeObserver.OnGlobalLayoutListener,
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

internal fun View.monitorExposureState(viewExposureController: IViewExposureController?) {
    val original = this.getTag(R.id.view_exposure_listener_holder) as? ExposeListener
    if (original != null) {
        if (this.isAttachedToWindow) {
            original.onViewDetachedFromWindow(this)
        }
        this.removeOnAttachStateChangeListener(original)
    }
    if (viewExposureController == null) {
        return
    }
    onViewLayoutChange(this, viewExposureController)
    val listener = ExposeListener(this, viewExposureController)
    if (this.isAttachedToWindow) {
        listener.onViewAttachedToWindow(this)
    }
    this.addOnAttachStateChangeListener(listener)
    this.setTag(R.id.view_exposure_listener_holder, listener)
}

internal fun IViewExposureController.forceCheck(view: View) {
    onViewLayoutChange(view, this)
}

private fun onViewLayoutChange(v: View, exposureController: IViewExposureController, attachState: Boolean? = null) {
    val traceItem = exposureController.traceable ?: return
    exposureController.stateManager?.also { stateHelper ->
        val exposing = isViewExposing(v, attachState)
        stateHelper.markExposeState(traceItem, exposing)
    }
}

///////////////////////////////////////////////////////////////////////////
// following below is check the view state
///////////////////////////////////////////////////////////////////////////

private val View.actualAlpha: Float
    get() {
        var r = 1.0F
        traverseAncestor { r *= it.alpha }
        return r
    }

private val View.isActualVisible: Boolean
    get() {
        traverseAncestor {
            if (!it.isVisible) {
                return false
            }
        }
        return true
    }

private fun isViewExposing(v: View, attachState: Boolean?): Boolean {
    if (!v.isActualVisible) {
        return false
    }
    if (v.actualAlpha < 0.01) {
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

private fun checkPointShowing(x: Int, y: Int, v: View, out: IntArray): Boolean {
    v.traverseAncestor { view ->
        view.getLocationOnScreen(out)
        val vx = out[0]
        val vy = out[1]
        if (x !in vx..vx + view.width || y !in vy..vy + view.height) {
            return false
        }
    }
    return true
}

private inline fun View.traverseAncestor(withThis: Boolean = true, block: (View) -> Unit) {
    var view: View? = this
    while (view != null) {
        if (withThis || view !== this) {
            block(view)
        }
        view = view.parent as? View
    }
}
