package com.rtnutils;
import com.rtnutils.utils.IconUtils

import com.rtnutils.NativeGetRtnUtilsSpec
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
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
        private const val E_ACTION_IS_EMPTY = "E_ACTION_IS_EMPTY"
        private const val E_INTENT_IS_NULL = "E_INTENT_IS_NULL"
        private const val E_GET_ICON_APP = "E_GET_ICON_APP"
        private const val E_FAILED_TO_OPEN_SETTINGS = "E_FAILED_TO_OPEN_SETTINGS"
        private const val E_PACKAGE_NOT_FOUND = "E_PACKAGE_NOT_FOUND"
        private const val E_VALIDATION_FAILS = "E_VALIDATION_FAILS"
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
            promise.reject(E_ACTION_IS_EMPTY, "Action is empty.")
            return
        }

        try {
            val settingsIntent = Intent(action)
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            currentActivity.startActivity(settingsIntent)
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject(E_FAILED_TO_OPEN_SETTINGS, "Failed to open settings: ${e.message}")
        }
    }

    override fun getLocationApps(options: ReadableMap?, promise: Promise) {
        val includesBase64 = if (options?.hasKey("includesBase64") == true) options.getBoolean("includesBase64") else false    
        val pm = reactApplicationContext.packageManager
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="))
    
        if (intent == null) {
            promise.reject(E_INTENT_IS_NULL, "Intent is null.")
            return
        }
    
        val appsList = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    
        if (appsList.isEmpty()) {
            promise.resolve(WritableNativeArray())
            return
        }
    
        val result = WritableNativeArray()
    
        for (resolveInfo in appsList) {
            val appInfo = resolveInfo.activityInfo.applicationInfo
            val appName = pm.getApplicationLabel(appInfo).toString()
            val packageName = appInfo.packageName
    
            val appData = WritableNativeMap().apply {
                putString("name", appName)
                putString("package", packageName)

                if (includesBase64) {
                    try {
                        val appIcon = pm.getApplicationIcon(packageName)
                        putString("icon", IconUtils.getAppIconBase64(appIcon))
                    } catch (e: Exception) {
                        promise.reject(E_GET_ICON_APP, "Error while getting app icon for package: $packageName", e)
                    }
                }
            }
            result.pushMap(appData)
        }
        promise.resolve(result)
    }

    override fun openAppWithLocation(options: ReadableMap?, promise: Promise) {
        if (options == null) {
            promise.reject(E_VALIDATION_FAILS, "Options are required.")
            return
        }
    
        val url = options.getString("url")
        val packageName = options.getString("packageName")
    
        if (url == null) {
            promise.reject(E_VALIDATION_FAILS, "URL is required.")
            return
        }
    
        if (packageName == null) {
            promise.reject(E_VALIDATION_FAILS, "PackageName is required.")
            return
        }
    
        val pm = reactApplicationContext.packageManager
    
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        } catch (e: PackageManager.NameNotFoundException) {
            promise.reject(E_PACKAGE_NOT_FOUND, "App not found for package: $packageName")
            return
        }
    
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setPackage(packageName)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    
        if (intent.resolveActivity(pm) != null) {
            try {
                reactApplicationContext.startActivity(intent)
                promise.resolve("App opened successfully.")
            } catch (e: Exception) {
                promise.reject("openAppWithLocationError", "Failed to open app with location", e)
            }
        } else {
            promise.reject(E_INTENT_IS_NULL, "App does not support the URL scheme.")
        }
    }


    private fun ReadableMap.getString(key: String): String? {
        return if (hasKey(key) && getType(key) == ReadableType.String) getString(key) else null
    }
}
