package com.example.servingwebcontent.model

sealed class TransferResult {
    data class Success(val fromAccountId: String, val toAccountId: String, val amount: Int) : TransferResult()

    data class InsufficientFunds(val fromAccountId: String, val availableBalance: Int, val requestedAmount: Int) :
        TransferResult()

    sealed class InvalidInput : TransferResult() { // make new file
        data class InvalidAccount(val accountId: String) : InvalidInput()
        data object InvalidAmount : InvalidInput()
    }
}