package com.example.homebuttontrigger.utils

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import android.util.Log


object ContactHelper {
    fun getPhoneNumberByName(context: Context, name: String): String? {
        val contentResolver: ContentResolver = context.contentResolver
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)

        // Use LOWER() to make the search case-insensitive
        val selection = "LOWER(${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME}) = LOWER(?)"
        val selectionArgs = arrayOf(name)

        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                Log.i("OnDevice", "getPhoneNumberByName: ${it.getString(0)}")
                it.getString(0) // Return the first phone number found
            } else {
                null // No contact found
            }
        }
    }
}