package com.blackRock.service;

import com.blackRock.dto.PeriodResult;
import com.blackRock.dto.ReturnsRequest;
import com.blackRock.dto.ReturnsResponse;
import com.blackRock.model.KPeriod;
import com.blackRock.model.PPeriod;
import com.blackRock.model.QPeriod;
import com.blackRock.model.Transaction;
import com.blackRock.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ReturnsService {

    private static final double NPS_RATE = 0.0711;
    private static final double INDEX_RATE = 0.1449;
    private static final int RETIREMENT_AGE = 60;

    @Autowired
    private TaxService taxService;

    public ReturnsResponse calculateNPSReturns(ReturnsRequest request) {
        long startTime = System.nanoTime();
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        List<Transaction> processedTransactions = applyRules(request);
        List<PeriodResult> results = calculatePeriodReturns(
                request, processedTransactions, NPS_RATE, true);

        ReturnsResponse response = new ReturnsResponse();
        response.setInstrument("NPS");
        response.setResults(results);
        response.setPerformance(calculatePerformance(startTime, startMemory));

        return response;
    }

    public ReturnsResponse calculateIndexReturns(ReturnsRequest request) {
        long startTime = System.nanoTime();
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        List<Transaction> processedTransactions = applyRules(request);
        List<PeriodResult> results = calculatePeriodReturns(
                request, processedTransactions, INDEX_RATE, false);

        ReturnsResponse response = new ReturnsResponse();
        response.setInstrument("INDEX_FUND");
        response.setResults(results);
        response.setPerformance(calculatePerformance(startTime, startMemory));

        return response;
    }

    private List<Transaction> applyRules(ReturnsRequest request) {
        List<Transaction> transactions = new ArrayList<>(request.getTransactions());

        // Sort transactions by date for sweep line algorithm
        transactions.sort(Comparator.comparing(Transaction::getTimestamp));

        // Prepare period data
        List<QPeriod> qPeriods = request.getQ() != null ? request.getQ() : new ArrayList<>();
        List<PPeriod> pPeriods = request.getP() != null ? request.getP() : new ArrayList<>();

        // Sort periods
        qPeriods.sort(Comparator.comparing(QPeriod::getStart));
        pPeriods.sort(Comparator.comparing(PPeriod::getStart));

        // Apply q and p rules using sweep line algorithm
        PriorityQueue<QPeriod> activeQPeriods = new PriorityQueue<>(
                (a, b) -> b.getStart().compareTo(a.getStart()) // Latest start first
        );

        PriorityQueue<PPeriod> activePPeriods = new PriorityQueue<>(
                Comparator.comparing(PPeriod::getEnd)
        );

        int qIndex = 0, pIndex = 0;
        double currentPExtra = 0;

        for (Transaction transaction : transactions) {
            LocalDateTime date = transaction.getTimestamp();

            // Add new q periods that start on or before this date
            while (qIndex < qPeriods.size() &&
                    !qPeriods.get(qIndex).getStart().isAfter(date)) {
                QPeriod q = qPeriods.get(qIndex);
                if (!q.getEnd().isBefore(date)) {
                    activeQPeriods.offer(q);
                }
                qIndex++;
            }

            // Remove q periods that ended before this date
            activeQPeriods.removeIf(q -> q.getEnd().isBefore(date));

            // Add new p periods
            while (pIndex < pPeriods.size() &&
                    !pPeriods.get(pIndex).getStart().isAfter(date)) {
                PPeriod p = pPeriods.get(pIndex);
                if (!p.getEnd().isBefore(date)) {
                    activePPeriods.offer(p);
                    currentPExtra += p.getExtra();
                }
                pIndex++;
            }

            // Remove p periods that ended
            while (!activePPeriods.isEmpty() &&
                    activePPeriods.peek().getEnd().isBefore(date)) {
                PPeriod expired = activePPeriods.poll();
                currentPExtra -= expired.getExtra();
            }

            // Apply q rule (if any)
            if (!activeQPeriods.isEmpty()) {
                QPeriod latestQ = activeQPeriods.peek();
                transaction.setFinalRemanent(latestQ.getFixed());
            }

            // Apply p rule (add all extras)
            transaction.setFinalRemanent(transaction.getFinalRemanent() + currentPExtra);
        }

        return transactions;
    }

    private List<PeriodResult> calculatePeriodReturns(
            ReturnsRequest request,
            List<Transaction> transactions,
            double rate,
            boolean isNPS) {

        List<PeriodResult> results = new ArrayList<>();
        double annualIncome = request.getWage() * 12;
        int years = Math.max(5, RETIREMENT_AGE - request.getAge());

        for (KPeriod period : request.getK()) {
            double periodSum = 0;

            // Sum final remanents for this period
            for (Transaction t : transactions) {
                if (DateTimeUtil.isBetween(t.getTimestamp(), period.getStart(), period.getEnd())) {
                    periodSum += t.getFinalRemanent();
                }
            }

            PeriodResult result = new PeriodResult();
            result.setStart(period.getStart());
            result.setEnd(period.getEnd());
            result.setAmount(periodSum);

            if (isNPS) {
                // NPS calculation
                double futureValue = periodSum * Math.pow(1 + NPS_RATE, years);
                double inflationAdjusted = futureValue / Math.pow(1 + request.getInflation()/100, years);
                double taxBenefit = taxService.calculateTaxBenefit(annualIncome, periodSum);

                result.setProfit(inflationAdjusted - periodSum);
                result.setTaxBenefit(taxBenefit);
            } else {
                // Index fund calculation
                double futureValue = periodSum * Math.pow(1 + INDEX_RATE, years);
                double inflationAdjusted = futureValue / Math.pow(1 + request.getInflation()/100, years);

                result.setReturns(inflationAdjusted);
            }

            results.add(result);
        }

        return results;
    }

    private ReturnsResponse.PerformanceMetrics calculatePerformance(long startTime, long startMemory) {
        long endTime = System.nanoTime();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        long memoryUsedKB = (endMemory - startMemory) / 1024;

        ReturnsResponse.PerformanceMetrics metrics =
                new ReturnsResponse.PerformanceMetrics();

        // Format time as "HH:mm:ss.SSS"
        long hours = durationMs / 3600000;
        long minutes = (durationMs % 3600000) / 60000;
        long seconds = (durationMs % 60000) / 1000;
        long millis = durationMs % 1000;

        metrics.setTime(String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis));
        metrics.setMemory(String.format("%.2f MB", memoryUsedKB / 1024.0));
        metrics.setThreads(Runtime.getRuntime().availableProcessors());

        return metrics;
    }
}