package com.example.servingwebcontent.component

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TransferHandlerTest {
    private lateinit var transferHandler: TransferHandler
    @BeforeEach
    fun setUp() {
        transferHandler = TransferHandler()
    }
    @Test
    fun `test concurrent transfers with multiple threads`() {
        val fromAccountId = "1234567890"
        val fromInitialBalance = 100000

        val toAccountId = "0987654321"
        val toInitialBalance = 200000

        val transferAmount = 1000

        // Ensure accounts start with the expected balances
        assertEquals(fromInitialBalance, transferHandler.getBalance(fromAccountId))
        assertEquals(toInitialBalance, transferHandler.getBalance(toAccountId))

        val numberOfThreads = 10
        val executorService = Executors.newFixedThreadPool(numberOfThreads)

        // Simulate multiple threads transferring money from one account to another
        for (i in 1..numberOfThreads) {
            executorService.submit {
                transferHandler.transferFunds(fromAccountId, toAccountId, transferAmount)
            }
        }

        // Shutdown the executor and wait for all tasks to complete
        executorService.shutdown()
        executorService.awaitTermination(1, TimeUnit.MINUTES)

        // Check final balances after all transfers
        val expectedFromBalance = fromInitialBalance - (numberOfThreads * transferAmount)
        val expectedToBalance = toInitialBalance + (numberOfThreads * transferAmount)

        assertEquals(expectedFromBalance, transferHandler.getBalance(fromAccountId))
        assertEquals(expectedToBalance, transferHandler.getBalance(toAccountId))
    }

}