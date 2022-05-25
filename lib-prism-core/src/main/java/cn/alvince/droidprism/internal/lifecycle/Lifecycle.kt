package cn.alvince.droidprism.internal.lifecycle

import androidx.annotation.RestrictTo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class LifecycleObserver(owner: LifecycleOwner, val onStateChange: (source: LifecycleOwner, event: Lifecycle.Event) -> Unit): LifecycleEventObserver {

    private val target: LifecycleOwner? get() = _source.get()

    private val _source = WeakReference(owner)

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        onStateChange(source, event)
        if (event == Lifecycle.Event.ON_DESTROY) {
            source.lifecycle.removeObserver(this)
        }
    }

    fun attach() {
        target?.lifecycle?.addObserver(this)
    }
}

fun LifecycleOwner.observeWith(onStateChange: (source: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    LifecycleObserver(this, onStateChange).attach()
}
