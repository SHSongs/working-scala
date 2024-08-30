package com.example.servingwebcontent.controller


import com.example.servingwebcontent.model.ScheduleTransferResult
import com.example.servingwebcontent.model.ScheduledTransferRequest
import com.example.servingwebcontent.model.TransferResult
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
            return when (val result = scheduledTransferService.scheduleTransfer(transferRequest)) {
                is ScheduleTransferResult.InsufficientFunds -> {
                    ResponseEntity(
                        "Insufficient funds: Available balance is ${result.availableBalance}, but requested ${result.requestedAmount}",
                        HttpStatus.BAD_REQUEST
                    )
                }

                is ScheduleTransferResult.InvalidInput.PastScheduledTime -> {
                    ResponseEntity(
                        "Scheduled time must be in the future. Provided time: ${result.scheduledTime}",
                        HttpStatus.BAD_REQUEST
                    )
                }

                is ScheduleTransferResult.InvalidInput.GeneralCase -> {
                    when (val invalidInput = result.invalidInput) {
                        is TransferResult.InvalidInput.InvalidAccount -> {
                            ResponseEntity("Invalid account ID: ${invalidInput.accountId}", HttpStatus.BAD_REQUEST)
                        }

                        is TransferResult.InvalidInput.InvalidAmount -> {
                            ResponseEntity("Transfer amount must be greater than zero", HttpStatus.BAD_REQUEST)
                        }
                    }
                }

                is ScheduleTransferResult.Success -> {
                    ResponseEntity("Transfer scheduled successfully", HttpStatus.OK)
                }

            }

        } catch (e: Exception) {
            // todo: logging
            ResponseEntity("unknown error", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
