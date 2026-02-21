package com.blackRock.dto;

import com.blackRock.model.KPeriod;
import com.blackRock.model.PPeriod;
import com.blackRock.model.QPeriod;
import com.blackRock.model.Transaction;
import lombok.Data;

import java.util.List;

@Data
public  class FilterRequest {
    private List<QPeriod> q;
    private List<PPeriod> p;
    private List<KPeriod> k;
    private List<Transaction> transactions;
}
