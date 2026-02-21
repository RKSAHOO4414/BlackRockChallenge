package com.blackRock.controller;

import com.blackRock.dto.*;
import com.blackRock.model.*;
import com.blackRock.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/blackrock/challenge/v1/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/parse")
    public ResponseEntity<ParseResponse> parseTransactions(
            @Valid @RequestBody ParseRequest request) {

        ParseResponse response = transactionService.parseExpenses(request.getExpenses());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validator")
    public ResponseEntity<ValidationResponse> validateTransactions(
            @RequestBody ValidationRequest request) {

        List<Transaction> valid = new ArrayList<>();
        List<InvalidTransaction> invalid = new ArrayList<>();
        List<InvalidTransaction> duplicates = new ArrayList<>();

        double annualIncome = request.getWage() * 12;
        double maxInvestment = Math.min(annualIncome * 0.10, 200000); // 10% of income or 2L max

        Set<LocalDateTime> seenTimestamps = new HashSet<>();

        for (Transaction t : request.getTransactions()) {
            List<String> errors = new ArrayList<>();

            // Check duplicate timestamp
            if (seenTimestamps.contains(t.getTimestamp())) {
                duplicates.add(new InvalidTransaction(
                        t.getTimestamp(), t.getAmount(), "Duplicate timestamp"));
                continue;
            }

            // Validate amount range
            if (t.getAmount() <= 0 || t.getAmount() >= 500000) {
                errors.add("Amount must be between 0 and 500,000");
            }

            // Validate ceiling calculation
            double expectedCeiling = Math.ceil(t.getAmount() / 100) * 100;
            if (Math.abs(expectedCeiling - t.getCeiling()) > 0.01) {
                errors.add("Invalid ceiling calculation");
            }

            // Validate remanent
            double expectedRemanent = expectedCeiling - t.getAmount();
            if (Math.abs(expectedRemanent - t.getRemanent()) > 0.01) {
                errors.add("Invalid remanent calculation");
            }

            // Validate against wage limit
            if (t.getRemanent() > maxInvestment) {
                errors.add("Investment exceeds 10% of annual income or 2L limit");
            }

            if (errors.isEmpty()) {
                valid.add(t);
                seenTimestamps.add(t.getTimestamp());
            } else {
                invalid.add(new InvalidTransaction(
                        t.getTimestamp(), t.getAmount(), String.join(", ", errors)));
            }
        }

        ValidationResponse response = new ValidationResponse(valid, invalid, duplicates);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/filter")
    public ResponseEntity<FilterResponse> filterTransactions(
            @RequestBody FilterRequest request) {

        List<Transaction> valid = new ArrayList<>();
        List<InvalidTransaction> invalid = new ArrayList<>();

        // Get min and max dates from transactions
        LocalDateTime minDate = request.getTransactions().stream()
                .map(Transaction::getTimestamp)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime maxDate = request.getTransactions().stream()
                .map(Transaction::getTimestamp)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        for (Transaction transaction : request.getTransactions()) {
            List<String> errors = new ArrayList<>();
            LocalDateTime date = transaction.getTimestamp();

            // Validate q periods
            if (request.getQ() != null) {
                for (int i = 0; i < request.getQ().size(); i++) {
                    QPeriod q = request.getQ().get(i);

                    // Check if period is within expense range
                    if (q.getStart().isBefore(minDate) || q.getEnd().isAfter(maxDate)) {
                        errors.add("q period " + i + " is outside expense date range");
                    }

                    // Check start <= end
                    if (q.getStart().isAfter(q.getEnd())) {
                        errors.add("q period " + i + " has invalid start/end dates");
                    }
                }
            }

            // Validate p periods
            if (request.getP() != null) {
                for (int i = 0; i < request.getP().size(); i++) {
                    PPeriod p = request.getP().get(i);

                    if (p.getStart().isBefore(minDate) || p.getEnd().isAfter(maxDate)) {
                        errors.add("p period " + i + " is outside expense date range");
                    }

                    if (p.getStart().isAfter(p.getEnd())) {
                        errors.add("p period " + i + " has invalid start/end dates");
                    }
                }
            }

            // Validate k periods
            if (request.getK() != null) {
                for (int i = 0; i < request.getK().size(); i++) {
                    KPeriod k = request.getK().get(i);

                    if (k.getStart().isBefore(minDate) || k.getEnd().isAfter(maxDate)) {
                        errors.add("k period " + i + " is outside expense date range");
                    }

                    if (k.getStart().isAfter(k.getEnd())) {
                        errors.add("k period " + i + " has invalid start/end dates");
                    }
                }
            }

            if (errors.isEmpty()) {
                valid.add(transaction);
            } else {
                invalid.add(new InvalidTransaction(
                        transaction.getTimestamp(),
                        transaction.getAmount(),
                        String.join(", ", errors)));
            }
        }

        FilterResponse response = new FilterResponse(valid, invalid);
        return ResponseEntity.ok(response);
    }
}