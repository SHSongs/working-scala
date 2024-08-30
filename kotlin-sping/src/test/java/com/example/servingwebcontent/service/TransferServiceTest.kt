package com.example.servingwebcontent.service

import com.example.servingwebcontent.model.TransferRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class TransferServiceTest {

    @Autowired
    lateinit var transferService: TransferService

    @Test
    fun `test successful transfer`() {
        val initialBalance = transferService.getBalance("1234567890")!!
        transferService.transferFunds(TransferRequest("1234567890", "0987654321", 50000))
        assertEquals(initialBalance - 50000, transferService.getBalance("1234567890"))
    }

    @Test
    fun `test insufficient funds`() {
        val exception = assertThrows<IllegalArgumentException> {
            transferService.transferFunds(TransferRequest("1234567890", "0987654321", 200000))
        }
        assertEquals("Insufficient funds", exception.message)
    }
}
