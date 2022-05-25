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

class ViewTraceHelper(internal val option: Option): Option.Observer {

    var trace: ITraceable? by NullableProperty { value -> applyTraceItemChanged(value) }

    var exposureStateHelper: ExposureStateHelper? by NullableProperty { value ->
        value?.createViewExposureHelper()
            ?.also {
                applyViewExposureHelper(it)
            }
    }

    private val targetView: View? get() = targetViewRef?.get()

    private var targetViewRef: WeakReference<View>? = null
    private var viewExposureHelper: ViewExposureHelper? = null

    override fun onOptionChanged(optionName: String) {
        TODO("Not yet implemented")
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
        viewExposureHelper
            ?.takeIf { option.enableExposureTrace }
            ?.also {
                view.monitorExposureState(it)
            }
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

    fun deactivate() {
        detachFromView()
        trace = null
    }

    private fun applyTraceItemChanged(trace: ITraceable?) {
        if (option.enableExposureTrace) {
            val exposureHelper = viewExposureHelper
                ?: exposureStateHelper?.run {
                    createViewExposureHelper()
                }
            exposureHelper?.trace = trace
        }
    }

    private fun applyViewExposureHelper(exposureHelper: ViewExposureHelper) {
        if (!option.enableExposureTrace) {
            return
        }
        if (viewExposureHelper === exposureHelper) {
            return
        }
        viewExposureHelper = exposureHelper
            .also {
                it.trace = this.trace
            }
        targetView?.monitorExposureState(exposureHelper)
    }

    companion object {

        @MainThread
        fun clear(view: View) {
            from(view)?.deactivate()
        }

        @MainThread
        fun traceClickIfNeeded(view: View) {
            from(view)
                ?.takeIf { it.option.enableClickTrace }
                ?.trace
                ?.also {
                    Instrumentation.eventEmitter.emitAction(ActionType.CLICK, it)
                }
        }

        fun from(view: View): ViewTraceHelper? {
            return view.getTag(R.id.view_trace_helper_holder) as? ViewTraceHelper
        }

        /**
         * Create [UniversalTraceHelper] instance with exposure enable specified
         */
        fun create(enableExposure: Boolean = true): ViewTraceHelper {
            return Option().apply {
                enableExposureTrace = enableExposure
            }.let {
                ViewTraceHelper(it)
            }
        }
    }
}

class Option {

    interface Observer {
        fun onOptionChanged(optionName: String)
    }

    var enableClickTrace: Boolean by ObservableProperty(true) { observer?.onOptionChanged("enableClickTrace") }

    var enableExposureTrace: Boolean by ObservableProperty(true) { observer?.onOptionChanged("enableExposureTrace") }

    internal var observer: Observer? = null
}
