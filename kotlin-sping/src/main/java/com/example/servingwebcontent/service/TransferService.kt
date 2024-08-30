package com.example.servingwebcontent.service

import com.example.servingwebcontent.model.TransferRequest
import com.example.servingwebcontent.model.TransferResult
import java.util.concurrent.atomic.AtomicInteger
import org.springframework.stereotype.Service

@Service
class TransferService {

    // Simulating account balances with an atomic variable (since no database is used)
    val accountBalances = mutableMapOf<String, AtomicInteger>()

    init {
        // Initialize some dummy accounts with balances
        accountBalances["1234567890"] = AtomicInteger(100000)
        accountBalances["0987654321"] = AtomicInteger(200000)
    }

    @Synchronized
    fun transferFunds(transferRequest: TransferRequest): TransferResult {
        val fromBalance = accountBalances[transferRequest.fromAccountId]
        val toBalance = accountBalances[transferRequest.toAccountId]
        if (fromBalance == null) {
            return TransferResult.InvalidInput.InvalidAccount(transferRequest.fromAccountId)
        }
        if (toBalance == null) {
            return TransferResult.InvalidInput.InvalidAccount(transferRequest.toAccountId)
        }

        if (transferRequest.amount <= 0) {
            return TransferResult.InvalidInput.InvalidAmount
        }


        if (fromBalance.get() < transferRequest.amount) {
            return TransferResult.InsufficientFunds(
                fromAccountId = transferRequest.fromAccountId,
                availableBalance = fromBalance.get(),
                requestedAmount = transferRequest.amount
            )
        }

        fromBalance.addAndGet(-transferRequest.amount)
        try {
            toBalance.addAndGet(transferRequest.amount)
        } catch (e: Exception) {
            // I prefer logging at this point (because the controller can not handle directly this error) and throw with wrapped abstract error
            fromBalance.addAndGet(transferRequest.amount)
            throw e
        }

        return TransferResult.Success(
            fromAccountId = transferRequest.fromAccountId,
            toAccountId = transferRequest.toAccountId,
            amount = transferRequest.amount
        )
    }

    fun getBalance(accountId: String): Int? { // kotlin의 규칙은 모르겠지만... return type이 optional도 get으로 하나?
        return accountBalances[accountId]?.get()
    }

}
