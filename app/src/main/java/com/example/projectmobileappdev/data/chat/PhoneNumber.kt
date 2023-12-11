package com.example.projectmobileappdev.data.chat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import android.util.Log

class PhoneNumberHelper(private val context: Context) {

    fun getPhoneNumber(): String {
        if(!hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            Log.e("PhoneNumberHelper", "READ phone state permission not granted")
        }
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        return try {
            val phoneNumber = telephonyManager.line1Number
            phoneNumber ?: "Phone number not available"
        } catch (e: SecurityException) {
            Log.e("PhoneNumberHelper", "Permission READ_PHONE_STATE not granted.")
            "Permission not granted"
        }
    }
    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}