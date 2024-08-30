package com.example.servingwebcontent.model

import java.time.LocalDateTime

sealed class TransferStatus {
    data object Success : TransferStatus()

    data class Failure(val reason: String) : TransferStatus()
    data class UnknownError(val reason: String) : TransferStatus()
}

data class ScheduledTransferResult(
    val request: ScheduledTransferRequest,
    val status: TransferStatus,
    val timestamp: LocalDateTime
)
