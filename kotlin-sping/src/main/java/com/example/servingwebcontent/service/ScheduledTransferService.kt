package com.example.servingwebcontent.service

import com.example.servingwebcontent.model.ScheduledTransferRequest
import com.example.servingwebcontent.model.TransferRequest
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentLinkedQueue

@Service
class ScheduledTransferService(private val transferService: TransferService) {
    private val logger = LoggerFactory.getLogger(ScheduledTransferService::class.java)

    private val scheduledTransfers = ConcurrentLinkedQueue<ScheduledTransferRequest>()

    fun scheduleTransfer(transferRequest: ScheduledTransferRequest): Unit {
        // check moeny...
        // double send?

        if (transferRequest.scheduledTime.isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("Scheduled time must be in the future")
        }
        scheduledTransfers.add(transferRequest)

        logger.info(
            "Scheduled transfer from {} to {} of amount {} at {}",
            transferRequest.fromAccountId,
            transferRequest.toAccountId,
            transferRequest.amount,
            transferRequest.scheduledTime
        )
    }

    @Scheduled(fixedRate = 6000)
    fun processScheduledTransfers() {
        val now = LocalDateTime.now()
        logger.info("Starting to process scheduled transfers at {}", now)

        val dueTransfers = getDueTransfers(now)

        // todo: send targets report to slack or datadog or log system

        for (transfer in dueTransfers) {
            try {
                transferService.transferFunds(
                    TransferRequest(transfer.fromAccountId, transfer.toAccountId, transfer.amount)
                )
                logger.info(
                    "Successfully processed transfer from {} to {} of amount {}",
                    transfer.fromAccountId,
                    transfer.toAccountId,
                    transfer.amount
                )
                removeTransfer(transfer)
            } catch (e: Exception) {
                logger.error(
                    "Failed to process transfer from {} to {} of amount {}. Error: {}",
                    transfer.fromAccountId,
                    transfer.toAccountId,
                    transfer.amount,
                    e.message
                )
                removeTransfer(transfer) // Optionally, remove failed transfer
            }
        }

        // send result

    }


    private fun getDueTransfers(now: LocalDateTime): List<ScheduledTransferRequest> {
        return scheduledTransfers.filter {
            it.scheduledTime.isBefore(now) || it.scheduledTime.isEqual(now)
        }
    }

    private fun removeTransfer(transfer: ScheduledTransferRequest) {
        scheduledTransfers.remove(transfer)
    }

}
