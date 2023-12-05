package com.example.projectmobileappdev.domain.chat

sealed interface ConnectionResult {
    object SuccessfulConnection: ConnectionResult

    data class TransferSucceeded(val message: BluetoothMessage): ConnectionResult
    data class Error(val errorMessage: String): ConnectionResult

}