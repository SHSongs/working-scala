package com.example.servingwebcontent.model

import java.time.LocalDateTime

data class ScheduledTransferRequest(
    val fromAccountId: String,
    val toAccountId: String,
    val amount: Int,
    val scheduledTime: LocalDateTime
)
