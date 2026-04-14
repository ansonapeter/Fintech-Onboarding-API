package com.example.lucidplus_onboarding.service;

import com.example.lucidplus_onboarding.dto.request.TransferRequest;
import com.example.lucidplus_onboarding.entity.Account;
import com.example.lucidplus_onboarding.entity.Transaction;
import com.example.lucidplus_onboarding.enums.TransactionType;
import com.example.lucidplus_onboarding.exception.AccountNotFoundException;
import com.example.lucidplus_onboarding.exception.InsufficientBalanceException;
import com.example.lucidplus_onboarding.repository.AccountRepository;
import com.example.lucidplus_onboarding.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public void transfer(TransferRequest request) {
        if (request.getSenderId().equals(request.getReceiverId()))
            throw new RuntimeException("Cannot transfer to yourself");

        Account sender = accountRepository.findByUserId(request.getSenderId())
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found"));

        Account receiver = accountRepository.findByUserId(request.getReceiverId())
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found"));

        if (sender.getBalance().compareTo(request.getAmount()) < 0)
            throw new InsufficientBalanceException("Insufficient balance");

        sender.setBalance(sender.getBalance().subtract(request.getAmount()));
        receiver.setBalance(receiver.getBalance().add(request.getAmount()));

        accountRepository.save(sender);
        accountRepository.save(receiver);


        transactionRepository.save(Transaction.builder()
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(request.getAmount())
                .type(TransactionType.DEBIT)
                .userId(request.getSenderId())
                .build());


        transactionRepository.save(Transaction.builder()
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(request.getAmount())
                .type(TransactionType.CREDIT)
                .userId(request.getReceiverId())
                .build());
    }

    public List<Transaction> getTransactions(Long userId) {
        return transactionRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}
