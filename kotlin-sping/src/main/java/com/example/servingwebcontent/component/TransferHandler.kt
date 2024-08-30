package com.example.servingwebcontent.component

import com.example.servingwebcontent.service.ScheduledTransferService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

@Component
class TransferHandler {
    private val accountBalances = mutableMapOf<String, AtomicInteger>()
    private val logger = LoggerFactory.getLogger(ScheduledTransferService::class.java)

    init {
        // Initialize some dummy accounts with balances
        accountBalances["1234567890"] = AtomicInteger(100000)
        accountBalances["0987654321"] = AtomicInteger(200000)
    }

    private val lockMap = ConcurrentHashMap<String, ReentrantLock>()

    fun transferFunds(fromAccountId: String, toAccountId: String, amount: Int): Unit {
        val fromLock = lockMap.computeIfAbsent(fromAccountId) { ReentrantLock() }
        val toLock = lockMap.computeIfAbsent(toAccountId) { ReentrantLock() }

        fromLock.lock()
        toLock.lock()
        try {
            val fromBalance = accountBalances[fromAccountId]
            val toBalance = accountBalances[toAccountId]
            if (fromBalance == null || toBalance == null) {
                throw IllegalArgumentException("Invalid account ID")
            }
            if (fromBalance.get() < amount) {
                throw IllegalArgumentException("Insufficient funds")
            }


            fromBalance.addAndGet(-amount)
            try {
                toBalance.addAndGet(amount)
            } catch (e: Exception) {
                // I prefer logging at this point (because the controller can not handle directly this error) and throw with wrapped abstract error

                fromBalance.addAndGet(amount)
                throw e
            }

            logger.info("Transferred {} from {} to {}", amount, fromAccountId, toAccountId)
        } finally {
          toLock.unlock()
          fromLock.unlock()
        }



    }

    fun getBalance(accountId: String): Int? {
        return accountBalances[accountId]?.get()
    }
}