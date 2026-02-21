package com.blackRock.service;

import com.blackRock.dto.ParseResponse;
import com.blackRock.model.Expense;
import com.blackRock.model.InvalidTransaction;
import com.blackRock.model.Transaction;
import com.blackRock.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public ParseResponse parseExpenses(List<Expense> expenses) {
        List<Transaction> validTransactions = new ArrayList<>();
        List<InvalidTransaction> invalidTransactions = new ArrayList<>();
        Set<LocalDateTime> seenTimestamps = new HashSet<>();

        double totalInvested = 0.0;
        double totalRemanent = 0.0;
        double totalExpenses = 0.0;

        for (Expense expense : expenses) {
            try {
                // Validate timestamp uniqueness
                if (seenTimestamps.contains(expense.getTimestamp())) {
                    invalidTransactions.add(new InvalidTransaction(
                            expense.getTimestamp(),
                            expense.getAmount(),
                            "Duplicate timestamp"
                    ));
                    continue;
                }

                // Validate amount range
                if (expense.getAmount() <= 0 || expense.getAmount() >= 5_00_000) {
                    invalidTransactions.add(new InvalidTransaction(
                            expense.getTimestamp(),
                            expense.getAmount(),
                            "Amount must be between 0 and 500,000"
                    ));
                    continue;
                }

                double ceiling = Math.ceil(expense.getAmount() / 100) * 100;
                double remanent = ceiling - expense.getAmount();

                Transaction transaction = new Transaction(
                        expense.getTimestamp(),
                        expense.getAmount(),
                        ceiling,
                        remanent
                );

                validTransactions.add(transaction);
                seenTimestamps.add(expense.getTimestamp());

                totalInvested += ceiling;
                totalRemanent += remanent;
                totalExpenses += expense.getAmount();

            } catch (Exception e) {
                log.error("Error processing expense: {}", e.getMessage());
                invalidTransactions.add(new InvalidTransaction(
                        expense.getTimestamp(),
                        expense.getAmount(),
                        "Processing error: " + e.getMessage()
                ));
            }
        }

        if (!validTransactions.isEmpty()) {
            transactionRepository.saveAll(validTransactions);
        }

        ParseResponse response = new ParseResponse();
        response.setValid(validTransactions);
        response.setInvalid(invalidTransactions);
        response.setTotalTransactions(validTransactions.size() + invalidTransactions.size());
        response.setTotalInvested(totalInvested);
        response.setTotalRemanent(totalRemanent);
        response.setTotalExpenses(totalExpenses);

        return response;
    }
}