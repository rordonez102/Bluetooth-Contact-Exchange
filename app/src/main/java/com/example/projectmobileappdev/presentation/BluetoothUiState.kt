package com.example.projectmobileappdev.presentation
import com.example.projectmobileappdev.domain.chat.BluetoothDevice
import com.example.projectmobileappdev.domain.chat.BluetoothMessage

//data class that represents the state of the scanned and paired devices.
data class BluetoothUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
    val messages: List<BluetoothMessage> = emptyList()
)
