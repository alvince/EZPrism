@file:JvmName("InternalUtils")

package cn.alvince.droidprism.internal

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.collection.SparseArrayCompat
import org.json.JSONObject

internal val mainHandler = Handler(Looper.getMainLooper())

internal const val LOG_TAG = "EZPrism"

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

internal fun checkMainThread() {
    if (Thread.currentThread() != Looper.getMainLooper().thread) {
        error("Non called from main-thread.")
    }
}

internal fun runOnMain(block: () -> Unit) {
    if (Looper.getMainLooper().thread === Thread.currentThread()) {
        block()
    } else {
        mainHandler.post(block)
    }
}

internal fun postOnMain(delayed: Long = 0L, block: () -> Unit) {
    if (delayed > 0L) {
        mainHandler.postDelayed(block, delayed)
        return
    }
    mainHandler.post(block)
}

internal operator fun JSONObject.plus(another: JSONObject): JSONObject = this
    .apply {
        another.keys().forEach { k -> putOpt(k, another.get(k)) }
    }
