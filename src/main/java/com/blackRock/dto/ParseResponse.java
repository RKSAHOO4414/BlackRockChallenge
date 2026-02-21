package com.blackRock.dto;

import com.blackRock.model.InvalidTransaction;
import com.blackRock.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParseResponse {

    private List<Transaction> valid;
    private List<InvalidTransaction> invalid;

    // Statistics
    private Integer totalTransactions;
    private Double totalInvested;
    private Double totalRemanent;
    private Double totalExpenses;
}