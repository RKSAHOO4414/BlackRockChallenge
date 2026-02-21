package com.blackRock.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InvalidTransaction extends Transaction {

    private String message;

    public InvalidTransaction(LocalDateTime timestamp, Double amount, String message) {
        super(timestamp, amount, 0.0, 0.0);
        this.message = message;
    }
}