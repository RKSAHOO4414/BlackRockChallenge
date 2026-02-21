package com.blackRock.dto;

import com.blackRock.model.KPeriod;
import com.blackRock.model.PPeriod;
import com.blackRock.model.QPeriod;
import com.blackRock.model.Transaction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnsRequest {

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age must be at most 100")
    private Integer age;

    @NotNull(message = "Monthly wage is required")
    @Min(value = 0, message = "Wage must be positive")
    private Double wage;

    @NotNull(message = "Inflation rate is required")
    @Min(value = 0, message = "Inflation must be positive")
    private Double inflation;

    @Valid
    private List<QPeriod> q;

    @Valid
    private List<PPeriod> p;

    @Valid
    private List<KPeriod> k;

    @Valid
    @NotNull(message = "Transactions list is required")
    private List<Transaction> transactions;
}