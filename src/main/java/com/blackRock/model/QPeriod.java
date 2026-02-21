package com.blackRock.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QPeriod extends Period {

    private Double fixed;

    public QPeriod(LocalDateTime start, LocalDateTime end, Double fixed) {
        super(start, end);
        this.fixed = fixed;
    }
}