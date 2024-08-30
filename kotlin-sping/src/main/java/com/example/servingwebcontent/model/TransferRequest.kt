package com.example.servingwebcontent.model

data class TransferRequest(
    val fromAccountId: String,
    val toAccountId: String,
    val amount: Int
)
