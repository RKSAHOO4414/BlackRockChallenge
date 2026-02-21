package com.blackRock.dto;

import com.blackRock.model.Transaction;
import lombok.Data;

import java.util.List;

@Data
public  class ValidationRequest {
    private Double wage;
    private List<Transaction> transactions;
}
