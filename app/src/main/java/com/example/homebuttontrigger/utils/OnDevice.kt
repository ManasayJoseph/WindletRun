package com.example.homebuttontrigger.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class OnDevice {
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private var pendingPhoneNumber: String? = null
    }


    fun call(phoneNumber: String) {
        val appContext = ActivityContextHolder.currentActivity // Get activity dynamically
        if (appContext == null) {
            Log.e("OnDevice", "No available activity context.")
            return
        }
        val contactPhoneNumber = ContactHelper.getPhoneNumberByName(appContext, phoneNumber)
        Log.i("OnDevice", "Calling $contactPhoneNumber...")
        if ((ContextCompat.checkSelfPermission(appContext, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED ) && (ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_CONTACTS)== PackageManager.PERMISSION_GRANTED)
        ) {
            // Request permission (this is async, do NOT startActivity here)
            if (contactPhoneNumber != null) {
                makeCall(appContext, contactPhoneNumber)
            }


        } else {
            // Permission already granted → Make the call immediately
            ActivityCompat.requestPermissions(
                appContext,
                arrayOf(Manifest.permission.CALL_PHONE,Manifest.permission.READ_CONTACTS),
                PERMISSION_REQUEST_CODE
            )
        }
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
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Needed if called from non-activity context
        }

        try {
            context.startActivity(callIntent)
        } catch (e: Exception) {
            Log.e("OnDevice", "Failed to start call intent: ${e.message}", e)
        }
    }



    fun openApp(appName: String) {
        Log.i("OnDevice", "Opening $appName...")
        // Implementation to open the app
    }

    fun send(name: String, message: String) {
        Log.i("OnDevice", "Sending message to $name: $message")
        // Implementation to send the message
    }
}
