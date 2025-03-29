package com.example.homebuttontrigger.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class OnDevice {
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private var pendingPhoneNumber: String? = null
    }

    val appContext = ActivityContextHolder.currentActivity
    fun call(input: String) {

        if (appContext == null) {
            Log.e("OnDevice", "No available activity context.")
            return
        }

        val phoneNumber = if (isValidPhoneNumber(input)) {
            input
        } else {
            ContactHelper.getPhoneNumberByName(appContext, input)
        }

        if (phoneNumber != null) {
            if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                makeCall(appContext, phoneNumber)
            } else {
                ActivityCompat.requestPermissions(
                    appContext,
                    arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS),
                    PERMISSION_REQUEST_CODE
                )
                pendingPhoneNumber = phoneNumber
            }
        } else {
            Log.e("OnDevice", "Unable to retrieve phone number for input: $input")
        }
    }
    private fun isValidPhoneNumber(input: String): Boolean {
        return Patterns.PHONE.matcher(input).matches()
    }



    fun handlePermissionResult(requestCode: Int, grantResults: IntArray) {
        val appContext = ActivityContextHolder.currentActivity // Get activity dynamically
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                pendingPhoneNumber?.let {
                    makeCall(appContext!!, it)
                    pendingPhoneNumber = null
                }
            } else {
                Log.e("OnDevice", "Call permission denied.")
            }
        }
    }

    private fun makeCall(context: Context, phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(callIntent)
        } catch (e: Exception) {
            Log.e("OnDevice", "Failed to start call intent: ${e.message}", e)
        }
    }




    private fun openAppByName(context: Context, appName: String) {
        val packageManager: PackageManager = context.packageManager
        val installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        Log.i("OnDevice","Reached Openapp by ame with $appName")
        // Search for the app by its name
        for (appInfo in installedApplications) {
            val label = packageManager.getApplicationLabel(appInfo).toString()
            if (label.equals(appName, ignoreCase = true)) {
                val launchIntent: Intent? = packageManager.getLaunchIntentForPackage(appInfo.packageName)
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                } else {
                    Toast.makeText(context, "Unable to launch $appName", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }

        // If the app is not found
        Toast.makeText(context, "$appName not found", Toast.LENGTH_SHORT).show()
    }



    fun openApp(appName: String) {
        Log.i("OnDevice", "Opening $appName...")
        if (ActivityContextHolder.currentActivity != null) {
            val appC = ActivityContextHolder.currentActivity
            if (appC != null) {
                openAppByName(appC, appName)
            }
        } else { Log.w("OnDevice","Error with $appContext")}
        // Implementation to open the app
    }

    fun send(name: String, message: String) {
        Log.i("OnDevice", "Sending message to $name: $message")
        // Implementation to send the message
    }
}
