package com.blackRock.service;

import org.springframework.stereotype.Service;

@Service
public class TaxService {

    private static final double STANDARD_DEDUCTION = 50_000;
    private static final double NPS_MAX_LIMIT = 2_00_000;

    public double calculateTaxBenefit(double annualIncome, double investedAmount) {
        // Calculate eligible NPS deduction
        double tenPercentOfIncome = annualIncome * 0.10;
        double npsDeduction = Math.min(investedAmount,
                Math.min(tenPercentOfIncome, NPS_MAX_LIMIT));

        // Calculate tax without deduction
        double taxWithoutDeduction = calculateTax(annualIncome - STANDARD_DEDUCTION);

        // Calculate tax with NPS deduction
        double taxableIncomeWithDeduction = annualIncome - STANDARD_DEDUCTION - npsDeduction;
        double taxWithDeduction = calculateTax(taxableIncomeWithDeduction);

        // Tax benefit is the difference
        return Math.max(0, taxWithoutDeduction - taxWithDeduction);
    }

    private double calculateTax(double taxableIncome) {
        if (taxableIncome <= 700_000) {
            return 0;
        }

        double tax = 0;

        if (taxableIncome > 700_000) {
            double slab = Math.min(taxableIncome, 1_000_000) - 700_000;
            tax += slab * 0.10;
        }

        if (taxableIncome > 1_000_000) {
            double slab = Math.min(taxableIncome, 1_200_000) - 1_000_000;
            tax += slab * 0.15;
        }

        if (taxableIncome > 1_200_000) {
            double slab = Math.min(taxableIncome, 1_500_000) - 1_200_000;
            tax += slab * 0.20;
        }

        if (taxableIncome > 1_500_000) {
            double slab = taxableIncome - 1_500_000;
            tax += slab * 0.30;
        }

        return tax;
    }
}