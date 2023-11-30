package com.example.projectmobileappdev.domain.chat

typealias BluetoothDeviceDomain = BluetoothDevice

//contains the name and mac address of the bluetooth device that is selected 
data class BluetoothDevice(
    val name: String?,
    val address: String
)
