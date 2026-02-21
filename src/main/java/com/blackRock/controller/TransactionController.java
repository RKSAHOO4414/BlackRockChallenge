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
        double maxInvestment = Math.min(annualIncome * 0.10, 200000);

        Set<LocalDateTime> seenTimestamps = new HashSet<>();

        for (Transaction t : request.getTransactions()) {
            List<String> errors = new ArrayList<>();

            if (seenTimestamps.contains(t.getTimestamp())) {
                duplicates.add(new InvalidTransaction(
                        t.getTimestamp(), t.getAmount(), "Duplicate timestamp"));
                continue;
            }

            if (t.getAmount() <= 0 || t.getAmount() >= 500000) {
                errors.add("Amount must be between 0 and 500,000");
            }

            double expectedCeiling = Math.ceil(t.getAmount() / 100) * 100;
            if (Math.abs(expectedCeiling - t.getCeiling()) > 0.01) {
                errors.add("Invalid ceiling calculation");
            }

            double expectedRemanent = expectedCeiling - t.getAmount();
            if (Math.abs(expectedRemanent - t.getRemanent()) > 0.01) {
                errors.add("Invalid remanent calculation");
            }

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

        LocalDateTime minDate = request.getTransactions().stream()
                .map(Transaction::getTimestamp)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime maxDate = request.getTransactions().stream()
                .map(Transaction::getTimestamp)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // VALIDATE PERIODS FIRST
        List<String> periodErrors = validatePeriods(request, minDate, maxDate);
        if (!periodErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new FilterResponse(new ArrayList<>(),
                            List.of(new InvalidTransaction(null, null, String.join("; ", periodErrors))))
            );
        }

        // STEP 2: APPLY Q AND P RULES TO EACH TRANSACTION
        for (Transaction transaction : request.getTransactions()) {
            LocalDateTime date = transaction.getTimestamp();
            double currentRemanent = transaction.getRemanent();

            // Apply Q rule: REPLACE with fixed amount if in any Q period
            QPeriod applicableQ = findApplicableQPeriod(date, request.getQ());
            if (applicableQ != null) {
                currentRemanent = applicableQ.getFixed();  // REPLACE the remanent
            }

            // Apply P rule: ADD all extra amounts from P periods
            double totalExtra = findTotalExtraFromPPeriods(date, request.getP());
            currentRemanent += totalExtra;

            // Create a new transaction with updated finalRemanent
            Transaction processedTransaction = new Transaction(
                    transaction.getTimestamp(),
                    transaction.getAmount(),
                    transaction.getCeiling(),
                    transaction.getRemanent()
            );
            processedTransaction.setFinalRemanent(currentRemanent);

            valid.add(processedTransaction);
        }

        FilterResponse response = new FilterResponse(valid, invalid);
        return ResponseEntity.ok(response);
    }

    private QPeriod findApplicableQPeriod(LocalDateTime date, List<QPeriod> qPeriods) {
        if (qPeriods == null || qPeriods.isEmpty()) {
            return null;
        }

        List<QPeriod> matchingPeriods = new ArrayList<>();

        // Find all Q periods containing this date
        for (QPeriod q : qPeriods) {
            if (!date.isBefore(q.getStart()) && !date.isAfter(q.getEnd())) {
                matchingPeriods.add(q);
            }
        }

        if (matchingPeriods.isEmpty()) {
            return null;
        }

        // Sort by start date descending (latest first)
        matchingPeriods.sort((a, b) -> {
            int startCompare = b.getStart().compareTo(a.getStart());
            if (startCompare != 0) {
                return startCompare;
            }
            // If same start date, use original order (first in list)
            return Integer.compare(qPeriods.indexOf(a), qPeriods.indexOf(b));
        });

        return matchingPeriods.get(0); // Return the latest-starting one
    }


     //Calculate total extra from all P periods containing this date
    private double findTotalExtraFromPPeriods(LocalDateTime date, List<PPeriod> pPeriods) {
        if (pPeriods == null || pPeriods.isEmpty()) {
            return 0.0;
        }

        double totalExtra = 0.0;

        for (PPeriod p : pPeriods) {
            if (!date.isBefore(p.getStart()) && !date.isAfter(p.getEnd())) {
                totalExtra += p.getExtra();
            }
        }

        return totalExtra;
    }

     //Validate all periods against transaction date range
    private List<String> validatePeriods(FilterRequest request,
                                         LocalDateTime minDate,
                                         LocalDateTime maxDate) {
        List<String> errors = new ArrayList<>();

        // Validate Q periods
        if (request.getQ() != null) {
            for (int i = 0; i < request.getQ().size(); i++) {
                QPeriod q = request.getQ().get(i);

                if (q.getStart().isAfter(q.getEnd())) {
                    errors.add("q period " + i + ": start must be before end");
                }

                //if (q.getStart().isBefore(minDate) || q.getEnd().isAfter(maxDate)) {
                //    errors.add("q period " + i + " outside transaction date range");
                //}
            }
        }

        // Validate P periods
        if (request.getP() != null) {
            for (int i = 0; i < request.getP().size(); i++) {
                PPeriod p = request.getP().get(i);

                if (p.getStart().isAfter(p.getEnd())) {
                    errors.add("p period " + i + ": start must be before end");
                }

                //if (p.getStart().isBefore(minDate) || p.getEnd().isAfter(maxDate)) {
                //    errors.add("p period " + i + " outside transaction date range");
                //}
            }
        }

        // Validate K periods
        if (request.getK() != null) {
            for (int i = 0; i < request.getK().size(); i++) {
                KPeriod k = request.getK().get(i);

                if (k.getStart().isAfter(k.getEnd())) {
                    errors.add("k period " + i + ": start must be before end");
                }

                //if (k.getStart().isBefore(minDate) || k.getEnd().isAfter(maxDate)) {
                //    errors.add("k period " + i + " outside transaction date range");
                //}
            }
        }

        return errors;
    }
}