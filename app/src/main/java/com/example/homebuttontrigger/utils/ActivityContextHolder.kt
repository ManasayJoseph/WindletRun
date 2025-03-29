package com.example.homebuttontrigger.utils

import android.app.Activity
import java.lang.ref.WeakReference

object ActivityContextHolder {
    private var activityReference: WeakReference<Activity>? = null

    var currentActivity: Activity?
        get() = activityReference?.get()
        set(activity) {
            activityReference = if (activity != null) WeakReference(activity) else null
        }
}
