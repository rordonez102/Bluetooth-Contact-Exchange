package com.example.projectmobileappdev.data.chat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.example.projectmobileappdev.domain.chat.BluetoothDeviceDomain

//converts bluetooth device to bluetooth device domain that contains name and mac address of the device.
@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain{
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}
