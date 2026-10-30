package com.example.data.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

private const val TAG = "AppCheckInit"

private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"
private const val FIRESTORE_DB_ID_RES = "firestore_database_id"

fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            val app = FirebaseApp.initializeApp(context)
            if (app == null) {
                val dbIdRes = context.resources.getIdentifier(
                    FIRESTORE_DB_ID_RES,
                    "string",
                    context.packageName
                )
                val projectId = if (dbIdRes != 0) {
                    context.getString(dbIdRes)
                } else {
                    "arek-surabaya-cloud"
                }

                val options = FirebaseOptions.Builder()
                    .setApplicationId(context.packageName)
                    .setProjectId(projectId)
                    .setApiKey("AIzaSyArekSurabayaFallbackKeyForInit001")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
        }

        if (FirebaseApp.getApps(context).isEmpty()) {
            Log.w(TAG, "No FirebaseApp initialized. Skipping Firebase App Check.")
            return
        }

        val firebaseApp = FirebaseApp.getInstance()
        val appCheck = FirebaseAppCheck.getInstance(firebaseApp)

        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)

            if (!debugToken.isNullOrEmpty()) {
                val persistenceKey = firebaseApp.persistenceKey
                val prefs = context.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                // Synchronous commit() is required before DebugAppCheckProviderFactory reads prefs.
                prefs.edit()
                    .putString(DEBUG_SECRET_KEY, debugToken)
                    .commit()
                Log.d(TAG, "App Check debug secret configured successfully.")
            } else {
                Log.w(TAG, "No debug token found in Intent extra.")
            }

            appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "DebugAppCheckProviderFactory installed.")
        } else {
            val resId = context.resources.getIdentifier(
                RECAPTCHA_SITE_KEY_RES,
                "string",
                context.packageName
            )
            if (resId != 0) {
                val siteKey = context.getString(resId)
                if (siteKey.isNotBlank()) {
                    appCheck.installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                    )
                    Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
                } else {
                    Log.w(TAG, "recaptcha_site_key resource string is blank.")
                }
            } else {
                Log.w(TAG, "recaptcha_site_key string resource not found in package.")
            }
        }
    } catch (e: Exception) {
        Log.w(TAG, "Firebase App Check initialization skipped: ${e.message}")
    }
}
