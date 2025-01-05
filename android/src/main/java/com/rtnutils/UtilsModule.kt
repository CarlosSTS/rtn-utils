package com.rtnutils;

import com.rtnutils.NativeGetRtnUtilsSpec
import android.app.Activity
import android.provider.Settings
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import com.facebook.react.bridge.*
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.turbomodule.core.interfaces.TurboModule

class UtilsModule(reactContext: ReactApplicationContext) : NativeGetRtnUtilsSpec(reactContext) {

    companion object {
        enum class SettingsAction {
            LOCATION,
            WIFI,
            BLUETOOTH,
            SOUND,
            DISPLAY
        }
        const val NAME = "RTNUtils"
        private const val AUTH_REQUEST = 18864
        private const val E_ACTIVITY_DOES_NOT_EXIST = "E_ACTIVITY_DOES_NOT_EXIST"
        private const val E_AUTH_CANCELLED = "E_AUTH_CANCELLED"
        private const val E_FAILED_TO_SHOW_AUTH = "E_FAILED_TO_SHOW_AUTH"
        private const val E_ONE_REQ_AT_A_TIME = "E_ONE_REQ_AT_A_TIME"
        private const val WITHOUT_AUTHENTICATION = "WITHOUT_AUTHENTICATION"
    }

    private val keyguardManager: KeyguardManager =
        reactContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

    private var authPromise: Promise? = null

    private val activityEventListener = object : BaseActivityEventListener() {
        override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode != AUTH_REQUEST || authPromise == null) return

            try {
                when (resultCode) {
                    Activity.RESULT_CANCELED -> {
                        authPromise?.reject(E_AUTH_CANCELLED, "User canceled the authentication.")
                    }
                    Activity.RESULT_OK -> {
                        authPromise?.resolve(true)
                    }
                    else -> {
                        authPromise?.reject(E_FAILED_TO_SHOW_AUTH, "Unknown result code: $resultCode")
                    }
                }
            } finally {
                authPromise = null
            }
        }
    }

    init {
        reactContext.addActivityEventListener(activityEventListener)
    }

    override fun getName() = NAME

    override fun isDeviceSecure(promise: Promise) {
        promise.resolve(keyguardManager.isDeviceSecure)
    }

    override fun authenticate(map: ReadableMap, promise: Promise) {
        if (authPromise != null) {
            promise.reject(E_ONE_REQ_AT_A_TIME, "Authentication already in progress.")
            return
        }

        val currentActivity = currentActivity
        if (currentActivity == null) {
            promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity does not exist.")
            return
        }

        if (!keyguardManager.isDeviceSecure) {
            promise.resolve(WITHOUT_AUTHENTICATION)
            return
        }

        val reason = map.getString("reason")
        val description = map.getString("description")

        authPromise = promise

        try {
            val authIntent = keyguardManager.createConfirmDeviceCredentialIntent(reason, description)
            if (authIntent != null) {
                currentActivity.startActivityForResult(authIntent, AUTH_REQUEST)
            } else {
                promise.reject(E_FAILED_TO_SHOW_AUTH, "Failed to create authentication intent.")
                authPromise = null
            }
        } catch (e: Exception) {
            promise.reject(E_FAILED_TO_SHOW_AUTH, e.message ?: "An unknown error occurred.")
            authPromise = null
        }
    }
   
    override fun openGlobalSettings(action: String, promise: Promise) {
        val currentActivity = currentActivity
    
        if (currentActivity == null) {
            promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity does not exist.")
            return
        }
        if(action.isEmpty()) {
            promise.reject("E_ACTION_IS_EMPTY", "Action is empty.")
            return
        }

        try {
            val settingsIntent = Intent(action)
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            currentActivity.startActivity(settingsIntent)
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject("E_FAILED_TO_OPEN_SETTINGS", "Failed to open settings: ${e.message}")
        }
    }

    private fun ReadableMap.getString(key: String): String? {
        return if (hasKey(key) && getType(key) == ReadableType.String) getString(key) else null
    }
}
