package com.example.projectmobileappdev.domain.chat

sealed interface ConnectionResult {
    object SuccessfulConnection: ConnectionResult
    data class Error(val errorMessage: String): ConnectionResult

}