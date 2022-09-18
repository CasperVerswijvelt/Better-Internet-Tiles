package be.casperverswijvelt.unifiedinternetqs.util

import android.content.Context

fun reportException (e: Throwable) {
    // Does nothing, no crashlytics in fdroid build flavor
}

fun initializeFirebase (context: Context, userId: String) {
    // Does nothing, no crashlytics in fdroid build flavor
}