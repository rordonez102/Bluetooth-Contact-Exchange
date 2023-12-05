package com.example.projectmobileappdev.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import com.example.projectmobileappdev.domain.chat.BluetoothController
import com.example.projectmobileappdev.domain.chat.BluetoothDeviceDomain
import com.example.projectmobileappdev.domain.chat.BluetoothMessage
import com.example.projectmobileappdev.domain.chat.ConnectionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

@SuppressLint("MissingPermission")
class AndroidBluetoothController(
    private val context: Context
): BluetoothController {
    //grabs the bluetooth manager and bluetooth adapter instance only when it is needed
    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }
    private var dataTransfer: BluetoothDataTransfer? = null

    private val phoneNumberHelper = PhoneNumberHelper(context)

    private val _isConnected = MutableStateFlow<Boolean>(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()
    //Stateflow for scanned and paired bluetooth devices.
    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()
    //handles the discovered devices and adds them to device list using a broadcast receiver
    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        _scannedDevices.update {devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if(newDevice in devices) devices else devices + newDevice
        }
    }

    private val bluetoothStateReceiver = BluetoothStateReceiver{ isConnected, bluetoothDevice ->
        if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
            _isConnected.update{ isConnected }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                _errors.emit("Can't connect to a non-paired device")
            }
        }
    }
    private var deviceServerSocket: BluetoothServerSocket? = null
    private var deviceClientSocket: BluetoothSocket? = null

    init {
        updatePairedDevices()
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
    }

    //starts the discovery process for bluetooth devices.
    override fun startDiscovery() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )
        updatePairedDevices()
        bluetoothAdapter?.startDiscovery()
    }
    //stops the discovery process for bluetooth devices.
    override fun stopDiscovery() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        bluetoothAdapter?.cancelDiscovery()
    }

    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }
            deviceServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "contact exchange",
                UUID.fromString(SERVICE_UUID)
            )
            var shouldLoop = true
            while (shouldLoop) {
                deviceClientSocket = try {
                    deviceServerSocket?.accept()
                } catch(e: IOException) {
                    shouldLoop = false
                    null
                }
                emit(ConnectionResult.SuccessfulConnection)
                deviceClientSocket?.let {
                    deviceServerSocket?.close()
                    val service = BluetoothDataTransfer(it)
                    dataTransfer = service
                    emitAll(
                        service
                            .listenForIncomingMessages()
                            .map {
                                ConnectionResult.TransferSucceeded(it)
                            }
                    )
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> {
        return flow {
            if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }
            deviceClientSocket = bluetoothAdapter
                ?.getRemoteDevice(device.address)
                ?.createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_UUID)
                )

            stopDiscovery()

            deviceClientSocket?.let {socket ->
                try {
                    socket.connect()
                    emit(ConnectionResult.SuccessfulConnection)

                    BluetoothDataTransfer(socket).also {
                        dataTransfer = it
                        emitAll(
                            it.listenForIncomingMessages()
                                .map { ConnectionResult.TransferSucceeded(it) }
                        )
                    }
                } catch(e:IOException){
                    socket.close()
                    deviceClientSocket = null
                    emit(ConnectionResult.Error("Connection was interrupted"))
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun trySendMessage(message: String?, imageUri: Uri?): BluetoothMessage? {
        if(!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return null
        }

        if(dataTransfer == null) {
            return null
        }
        //getPhoneNumberFlow()
        val bluetoothMessage = if (imageUri != null) {
            BluetoothMessage(
                imageBytes = readImageBytes(imageUri),
                senderName = bluetoothAdapter?.name ?: "Unknown name",
                isFromLocalUser = true
            )
        } else {
            BluetoothMessage(
                message = message,
                senderName = bluetoothAdapter?.name ?: "Unknown name",
                isFromLocalUser = true
            )
        }

        dataTransfer?.sendMessage(bluetoothMessage.toByteArray())

        return bluetoothMessage
    }
    override fun closeConnection() {
        deviceClientSocket?.close()
        deviceServerSocket?.close()
        deviceClientSocket = null
        deviceServerSocket = null
    }
    //releases resources by unregistering the found device receiver.
    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        context.unregisterReceiver(bluetoothStateReceiver)
        closeConnection()
    }

    //updates paired devices if permission is granted by calling the bluetooth adapter instance.
    private fun updatePairedDevices() {
        if (!hasPermission(android.Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        bluetoothAdapter
            ?.bondedDevices
            ?.map { it.toBluetoothDeviceDomain() }
            ?.also { devices ->
                _pairedDevices.update { devices } }
    }
    //checks if permission is granted.
    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
    private fun getPhoneNumberFlow(): String {
        return phoneNumberHelper.getPhoneNumber()
    }

    private suspend fun readImageBytes(uri: Uri): ByteArray {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: ByteArray(0)
                inputStream?.close()
                bytes
            } catch (e: IOException) {
                e.printStackTrace()
                ByteArray(0)
            }
        }
    }
    companion object {
        const val SERVICE_UUID = "27b7d1da-08c7-4505-a6d1-2459987e5e2d"
    }

}
