package com.example.servingwebcontent.controller

import com.example.servingwebcontent.model.TransferRequest
import com.example.servingwebcontent.model.TransferResult
import com.example.servingwebcontent.service.TransferService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class TransferController(private val transferService: TransferService) {

    @PostMapping("/transfer")
    fun transfer(@RequestBody transferRequest: TransferRequest): ResponseEntity<String> {
        return try {
            when (val result = transferService.transferFunds(transferRequest)) {
                is TransferResult.InsufficientFunds -> {
                    ResponseEntity(
                        "Insufficient funds: Available balance is ${result.availableBalance}, but requested ${result.requestedAmount}",
                        HttpStatus.BAD_REQUEST
                    )
                }

                is TransferResult.InvalidInput.InvalidAccount -> {
                    ResponseEntity("Invalid account ID: ${result.accountId}", HttpStatus.BAD_REQUEST)
                }

                is TransferResult.InvalidInput.InvalidAmount -> {
                    ResponseEntity("Transfer amount must be greater than zero", HttpStatus.BAD_REQUEST)
                }

                is TransferResult.Success -> {
                    ResponseEntity(
                        "Transfer successful from ${result.fromAccountId} to ${result.toAccountId} of amount ${result.amount}",
                        HttpStatus.OK
                    )
                }
            }
        } catch (e: Exception) {
            // todo: logging
            ResponseEntity("unknown error", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/balance/{accountId}")
    fun getBalance(@PathVariable accountId: String): ResponseEntity<Int> {
        val balance = transferService.getBalance(accountId)
        return if (balance != null) {
            ResponseEntity(balance, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }
}
