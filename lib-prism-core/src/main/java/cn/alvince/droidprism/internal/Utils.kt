package cn.alvince.droidprism.internal

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.collection.SparseArrayCompat

internal val mainHandler = Handler(Looper.getMainLooper())

private const val LOG_TAG = "EZPrism"

internal inline fun logDIfDebug(lazyMessage: () -> String) {
    if (Instrumentation.devMode) {
        Log.d(LOG_TAG, lazyMessage())
    }
}

internal inline fun logVIfDebug(lazyMessage: () -> String) {
    if (Instrumentation.devMode) {
        Log.v(LOG_TAG, lazyMessage())
    }
}

internal fun <T> SparseArrayCompat<T>.getOrPut(key: Int, supplier: () -> T): T {
    return get(key) ?: supplier().also { put(key, it) }
}

internal fun runOnMain(block: () -> Unit) {
    if (Looper.getMainLooper().thread == Thread.currentThread()) {
        block()
    } else {
        mainHandler.post(block)
    }
}
