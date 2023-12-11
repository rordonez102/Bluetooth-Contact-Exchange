package com.example.projectmobileappdev.domain.chat

data class BluetoothMessage(
    val message: String?= null,
    val imageBytes: ByteArray?= null,
    val senderName: String,
    val isFromLocalUser: Boolean
)
