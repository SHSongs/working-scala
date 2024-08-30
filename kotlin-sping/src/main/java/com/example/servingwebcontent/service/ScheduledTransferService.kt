package com.example.servingwebcontent.service

import com.example.servingwebcontent.model.*
import com.example.servingwebcontent.model.TransferResult.InvalidInput

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentLinkedQueue

@Service
class ScheduledTransferService(private val transferService: TransferService) {
    private val logger = LoggerFactory.getLogger(ScheduledTransferService::class.java)

    private val scheduledTransfers = ConcurrentLinkedQueue<ScheduledTransferRequest>()

    fun scheduleTransfer(transferRequest: ScheduledTransferRequest): ScheduleTransferResult {
        if (transferRequest.scheduledTime.isBefore(LocalDateTime.now())) {
            return ScheduleTransferResult.InvalidInput.PastScheduledTime(transferRequest.scheduledTime)
        }

        val fromBalance = transferService.accountBalances[transferRequest.fromAccountId]
        val toBalance = transferService.accountBalances[transferRequest.toAccountId]
        if (fromBalance == null) {
            return ScheduleTransferResult.InvalidInput.GeneralCase(InvalidInput.InvalidAccount(transferRequest.fromAccountId))
        }
        if (toBalance == null) {
            return ScheduleTransferResult.InvalidInput.GeneralCase(InvalidInput.InvalidAccount(transferRequest.toAccountId))
        }

        if (fromBalance.get() < transferRequest.amount) {
            return ScheduleTransferResult.InsufficientFunds(
                fromAccountId = transferRequest.fromAccountId,
                availableBalance = fromBalance.get(),
                requestedAmount = transferRequest.amount
            )
        }

        scheduledTransfers.add(transferRequest)

        logger.info(
            "Scheduled transfer from {} to {} of amount {} at {}",
            transferRequest.fromAccountId,
            transferRequest.toAccountId,
            transferRequest.amount,
            transferRequest.scheduledTime
        )
        return ScheduleTransferResult.Success
    }

    @Scheduled(fixedRate = 6000)
    fun processScheduledTransfers() {
        val now = LocalDateTime.now()
        logger.info("Starting to process scheduled transfers at {}", now)

        val dueTransfers = getDueTransfers(now)
        // todo: send targets report (include count) to slack or datadog or log system for debug

        val transferResults = mutableListOf<ScheduledTransferResult>()

        for (transfer in dueTransfers) {
            val result = try {
                val res = transferService.transferFunds(
                    TransferRequest(transfer.fromAccountId, transfer.toAccountId, transfer.amount)
                )
                when (res) {
                    is TransferResult.InsufficientFunds -> {
                        ScheduledTransferResult(
                            request = transfer,
                            status = TransferStatus.Failure("${res.requestedAmount}을 보내려 했지만 잔고가 부족해요. 현재잔고: ${res.availableBalance}"),
                            timestamp = now
                        )

                    }

                    is InvalidInput.InvalidAccount -> { // todo: refactor, 보내는 계좌인지 받는 계좌인지 구분
                        ScheduledTransferResult(
                            request = transfer,
                            status = TransferStatus.Failure("${res.accountId} 계좌를 찾을 수 없어요."),
                            timestamp = now
                        )
                    }

                    is InvalidInput.InvalidAmount -> { // todo: refactor and remove, 같은 레이어의 서비스 메서드를 사용하기 때문에 발생할 수 없는 에러까지 핸들해야함
                        ScheduledTransferResult(
                            request = transfer,
                            status = TransferStatus.Failure(""),
                            timestamp = now
                        )
                    }

                    is TransferResult.Success -> {
                        ScheduledTransferResult(
                            request = transfer, status = TransferStatus.Success, timestamp = now
                        )
                    }
                }
            } catch (e: Exception) {
                logger.error(
                    "Failed to process transfer from {} to {} of amount {}. Error: {}",
                    transfer.fromAccountId,
                    transfer.toAccountId,
                    transfer.amount,
                    e.message
                )
                ScheduledTransferResult(
                    request = transfer,
                    status = TransferStatus.UnknownError("서버에 알 수 없는 에러가 발생했어요. 계속된다면 ...."),
                    timestamp = now
                )
            }

            transferResults.add(result)
            removeTransfer(transfer) // 실패 시, 원인에 따라 재시도 로직을 넣을 수 있다.
        }

        // todo: sendTransferResultsReport(transferResults) or RegisterReport(transferResults) for user에게 전송 결과 전달
        println(transferResults.toList())
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
