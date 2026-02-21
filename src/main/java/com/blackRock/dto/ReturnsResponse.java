package com.blackRock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnsResponse {

    private String instrument;
    private List<PeriodResult> results;
    private PerformanceMetrics performance;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private String time;
        private String memory;
        private Integer threads;
    }
}