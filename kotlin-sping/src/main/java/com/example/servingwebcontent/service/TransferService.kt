package com.example.servingwebcontent.service

import com.example.servingwebcontent.model.TransferRequest
import java.util.concurrent.atomic.AtomicInteger
import org.springframework.stereotype.Service

@Service
class TransferService {

    // Simulating account balances with an atomic variable (since no database is used)
    private val accountBalances = mutableMapOf<String, AtomicInteger>()

    init {
        // Initialize some dummy accounts with balances
        accountBalances["1234567890"] = AtomicInteger(100000)
        accountBalances["0987654321"] = AtomicInteger(200000)
    }

    @Synchronized
    fun transferFunds(transferRequest: TransferRequest): Unit {
        val fromBalance = accountBalances[transferRequest.fromAccountId]
        val toBalance = accountBalances[transferRequest.toAccountId] // optional?

        if (fromBalance == null || toBalance == null) { // 이런면에서는 Scala가 더 추상화가 잘된듯
            throw IllegalArgumentException("Invalid account ID")
        }

        if (transferRequest.amount <= 0) {
            throw IllegalArgumentException("Transfer amount must be greater than zero")
        }

        if (fromBalance.get() < transferRequest.amount) {
            throw IllegalArgumentException("Insufficient funds")
        }

        fromBalance.addAndGet(-transferRequest.amount)
        try {
            toBalance.addAndGet(transferRequest.amount)
        } catch (e: Exception) {
            // I prefer logging at this point (because the controller can not handle directly this error) and throw with wrapped abstract error
            fromBalance.addAndGet(transferRequest.amount)
            throw e
        }

    }

    fun getBalance(accountId: String): Int? { // kotlin의 규칙은 모르겠지만... return type이 optional도 get으로 하나?
        return accountBalances[accountId]?.get()
    }
}
