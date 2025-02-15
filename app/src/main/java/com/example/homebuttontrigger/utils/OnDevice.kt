package com.example.homebuttontrigger.utils

import android.util.Log


class OnDevice {
    fun call(name: String) {
        Log.i("Ondevice","Calling $name...")
    }

    fun openApp(appName: String) {
        Log.i("Ondevice","Opening $appName...")
    }

    fun send(name: String, message: String) {
        Log.i("Ondevice","Sending message to $name: $message")
    }
}
