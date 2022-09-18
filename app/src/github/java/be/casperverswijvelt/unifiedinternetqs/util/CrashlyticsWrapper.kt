package be.casperverswijvelt.unifiedinternetqs.util

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

fun reportException (e: Throwable) {
    FirebaseCrashlytics.getInstance().recordException(e)
}

fun initializeFirebase (context: Context, userId: String) {
    FirebaseApp.initializeApp(context)
    FirebaseCrashlytics.getInstance().setUserId(userId)
}