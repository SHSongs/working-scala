package com.example.servingwebcontent.model

import java.time.LocalDateTime

sealed class ScheduleTransferResult {
    object Success : ScheduleTransferResult()

    data class InsufficientFunds(val fromAccountId: String, val availableBalance: Int, val requestedAmount: Int) :
        ScheduleTransferResult()

    sealed class InvalidInput : ScheduleTransferResult() {
        data class PastScheduledTime(val scheduledTime: LocalDateTime) : InvalidInput()

        data class GeneralCase(val invalidInput: TransferResult.InvalidInput) : InvalidInput()

    }
}