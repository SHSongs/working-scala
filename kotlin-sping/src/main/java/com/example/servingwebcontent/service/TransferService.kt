package com.example.servingwebcontent.service

import com.example.servingwebcontent.component.TransferHandler
import com.example.servingwebcontent.model.TransferRequest
import org.springframework.stereotype.Service

@Service
class TransferService(
    private val transferHandler: TransferHandler
) {

    fun transferFunds(transferRequest: TransferRequest): Unit {
        // validate input
        if (transferRequest.amount <= 0) {
            throw IllegalArgumentException("Transfer amount must be greater than zero")
        }
        transferHandler.transferFunds(transferRequest.fromAccountId, transferRequest.toAccountId, transferRequest.amount)
    }

    fun getBalance(accountId: String): Int? {
        // validate input
        return transferHandler.getBalance(accountId)
    }
}
