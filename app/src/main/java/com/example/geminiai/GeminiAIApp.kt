package com.example.geminiai

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize

/**
 * Application entry point: initializes Firebase and App Check (debug provider for local runs).
 */
class GeminiAIApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance(),
        )

        // Forces the debug provider to mint a token so Logcat prints the debug secret.
        Firebase.appCheck.getAppCheckToken(false)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "App Check ready — look for DebugAppCheckProvider debug secret above")
                } else {
                    Log.e(TAG, "Failed to fetch App Check token", task.exception)
                }
            }
    }

    companion object {
        private const val TAG = "GeminiAIApp"
    }
}
