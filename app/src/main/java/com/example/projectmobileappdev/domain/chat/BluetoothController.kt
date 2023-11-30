package com.example.projectmobileappdev.domain.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val isConnected: StateFlow<Boolean>
    // Stateflow that represents a list of scanned and paired bluetooth devices.
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    val pairedDevices: StateFlow<List<BluetoothDevice>>
    val errors: SharedFlow<String>
    //function that starts and stops the discovery process for bluetooth devices.
    fun startDiscovery()
    fun stopDiscovery()

    fun startBluetoothServer() : Flow<ConnectionResult>
    fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult>

    fun closeConnection()
    //function that releases any resources being used by the controller
    fun release()
}
