package com.example.projectmobileappdev.data.chat

import com.example.projectmobileappdev.domain.chat.BluetoothMessage

import android.util.Base64

fun String.toBluetoothMessage(isFromLocalUser: Boolean): BluetoothMessage {
    val name = substringBeforeLast("#")
    val message = substringAfter("#TEXT:")

    return if (contains("#IMAGE:")) {
        val imageBase64 = substringAfter("#IMAGE:")
        val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
        BluetoothMessage(
            message = null,
            imageBytes = imageBytes,
            senderName = name,
            isFromLocalUser = isFromLocalUser
        )
    } else {
        BluetoothMessage(
            message = message,
            imageBytes = null,
            senderName = name,
            isFromLocalUser = isFromLocalUser
        )
    }
}

fun BluetoothMessage.toByteArray(): ByteArray {
    return if (imageBytes != null) {
        "$senderName#IMAGE:${Base64.encodeToString(imageBytes, Base64.DEFAULT)}".encodeToByteArray()
    } else {
        "$senderName#TEXT:$message".encodeToByteArray()
    }
}