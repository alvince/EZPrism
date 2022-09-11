package cn.alvince.droidprism.operator

import android.view.View
import androidx.annotation.MainThread
import cn.alvince.droidprism.R
import cn.alvince.droidprism.internal.Instrumentation
import cn.alvince.droidprism.log.ActionType
import cn.alvince.droidprism.log.ExposureStateHelper
import cn.alvince.droidprism.log.ITraceable
import cn.alvince.zanpakuto.core.property.NullableProperty
import cn.alvince.zanpakuto.core.property.ObservableProperty
import java.lang.ref.WeakReference

/**
 * Universal [android.view.View] trace handler
 *
 * Created by alvince on 2022/5/23
 *
 * @author alvince.zy@gmail.com
 */
class ViewTraceHelper internal constructor(val option: ViewTraceOption) : ViewTraceOption.Observer, IViewExposureController {

    override val traceable: ITraceable? get() = trace

    override val stateManager: ExposureStateHelper? get() = exposureStateHelper

    var trace: ITraceable? by NullableProperty { value -> applyTraceItemChanged(value) }

    var exposureStateHelper: ExposureStateHelper? by NullableProperty { value ->
        value?.also { applyViewExposureHelper() }
    }

    private val targetView: View? get() = targetViewRef?.get()

    private var targetViewRef: WeakReference<View>? = null

    override fun onOptionChanged(optionName: String) {
        when (optionName) {
            "enableExposureTrace" -> {
                if (option.enableExposureTrace) {
                    exposureStateHelper?.also { applyViewExposureHelper() }
                } else {
                    // clear exposure helper
                    targetView?.monitorExposureState(null)
                }
            }
        }
    }

    fun attachToView(view: View) {
        val attached = from(view)
        if (attached === this) {
            return
        }
        attached?.detachFromView()
        // attach to new target
        view.setTag(R.id.view_trace_helper_holder, this)
        targetViewRef = WeakReference(view)
        if (option.enableExposureTrace) {
            view.monitorExposureState(this)
        }
    }

    fun deactivate() {
        detachFromView()
        trace = null
    }

    private fun detachFromView() {
        targetViewRef?.get()?.also { v ->
            // clear target trace helper
            v.setTag(R.id.view_trace_helper_holder, "")
            // clear exposure listener
            if (option.enableExposureTrace) {
                v.monitorExposureState(null)
            }
        }
    }

    private fun applyTraceItemChanged(trace: ITraceable?) {
        if (option.enableExposureTrace) {
            trace?.also { exposureStateHelper?.markExposeState(it, false) }
            targetView?.also { forceCheck(it) }
        }
    }

    private fun applyViewExposureHelper() {
        trace ?: return
        if (!option.enableExposureTrace) {
            return
        }
        targetView?.monitorExposureState(this)
    }

    companion object {

        @JvmStatic
        @MainThread
        fun clear(view: View) {
            from(view)?.deactivate()
        }

        @JvmStatic
        @MainThread
        fun traceClickIfNeeded(view: View) {
            from(view)
                ?.takeIf { it.option.enableClickTrace }
                ?.trace
                ?.also {
                    Instrumentation.eventEmitter.emitAction(ActionType.CLICK, it)
                }
        }

        @JvmStatic
        fun from(view: View): ViewTraceHelper? {
            return view.getTag(R.id.view_trace_helper_holder) as? ViewTraceHelper
        }

        /**
         * Create [ViewTraceHelper] instance with exposure enable specified
         */
        @JvmStatic
        fun create(enableExposure: Boolean = true): ViewTraceHelper {
            return ViewTraceOption().apply {
                enableExposureTrace = enableExposure
            }.let {
                ViewTraceHelper(it)
            }
        }
    }
}

class ViewTraceOption {

    interface Observer {
        fun onOptionChanged(optionName: String)
    }

    var enableClickTrace: Boolean by ObservableProperty(true) { observer?.onOptionChanged("enableClickTrace") }

    var enableExposureTrace: Boolean by ObservableProperty(true) { observer?.onOptionChanged("enableExposureTrace") }

    private var observer: Observer? = null
}
