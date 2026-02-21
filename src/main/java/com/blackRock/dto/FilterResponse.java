package com.blackRock.dto;

import com.blackRock.model.InvalidTransaction;
import com.blackRock.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public  class FilterResponse {
    private List<Transaction> valid;
    private List<InvalidTransaction> invalid;
}

