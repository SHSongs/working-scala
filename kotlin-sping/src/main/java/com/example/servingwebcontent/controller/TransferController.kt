package com.example.servingwebcontent.controller

import com.example.servingwebcontent.model.TransferRequest
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
            transferService.transferFunds(transferRequest)
            ResponseEntity("Transfer successful", HttpStatus.OK)
        } catch (e: IllegalArgumentException) {
            ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
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
