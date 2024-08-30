    
## 제약사항
- OS timezone은 KST
- KRW 만 다룹니다. 여러 통화를 다루려면 재설계가 필요합니다


## endpoint

### 잔고조회
```md
curl http://localhost:8080/api/v1/balance/1234567890
```

### 송금
```md
curl -X POST http://localhost:8080/api/v1/transfer \            
-H "Content-Type: application/json" \
-d '{
      "fromAccountId": "1234567890",
      "toAccountId": "0987654321",
      "amount": 50000
    }'
```


### 예약송금
```md
curl -X POST http://localhost:8080/api/v1/scheduleTransfer \
-H "Content-Type: application/json" \
-d '{
      "fromAccountId": "1234567890",
      "toAccountId": "0987654321",
      "amount": 50000,
      "scheduledTime": "2024-08-30T15:44:00"
    }'
```