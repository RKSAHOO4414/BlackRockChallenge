package com.blackRock.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeriodResult {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime start;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;

    private Double amount;      // Total invested in this period

    // For NPS
    private Double profit;       // Inflation-adjusted gain
    private Double taxBenefit;   // Tax benefit amount

    // For Index Fund
    private Double returns;      // Inflation-adjusted final amount
}