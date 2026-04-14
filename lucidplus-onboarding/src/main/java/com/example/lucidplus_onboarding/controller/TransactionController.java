package com.example.lucidplus_onboarding.controller;

import com.example.lucidplus_onboarding.dto.request.TransferRequest;
import com.example.lucidplus_onboarding.dto.response.ApiResponse;
import com.example.lucidplus_onboarding.entity.Transaction;
import com.example.lucidplus_onboarding.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<?>> transfer(@Valid @RequestBody TransferRequest request) {
        transactionService.transfer(request);
        return ResponseEntity.ok(ApiResponse.success("Transfer successful", null));
    }

    @GetMapping("/transactions/{userId}")
    public ResponseEntity<ApiResponse<?>> getTransactions(@PathVariable Long userId) {
        List<Transaction> transactions = transactionService.getTransactions(userId);
        return ResponseEntity.ok(ApiResponse.success("Transactions fetched", transactions));
    }
}
