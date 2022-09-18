package be.casperverswijvelt.unifiedinternetqs.util

import com.google.firebase.crashlytics.FirebaseCrashlytics

fun reportException (e: Throwable) {
    FirebaseCrashlytics.getInstance().recordException(e)
}

fun setCrashlyticsId (uuid: String) {
    FirebaseCrashlytics.getInstance().setUserId(uuid)
}