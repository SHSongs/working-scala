package com.example.servingwebcontent.controller


import com.example.servingwebcontent.model.ScheduledTransferRequest
import com.example.servingwebcontent.service.ScheduledTransferService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class ScheduledTransferController(private val scheduledTransferService: ScheduledTransferService) {

    @PostMapping("/scheduleTransfer")
    fun scheduleTransfer(@RequestBody transferRequest: ScheduledTransferRequest): ResponseEntity<String> {
        return try {
            scheduledTransferService.scheduleTransfer(transferRequest)
            ResponseEntity("Transfer scheduled successfully", HttpStatus.OK)
        } catch (e: IllegalArgumentException) {
            ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        }
    }
}
