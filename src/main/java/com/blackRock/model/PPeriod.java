package com.blackRock.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PPeriod extends Period {

    private Double extra;

    public PPeriod(LocalDateTime start, LocalDateTime end, Double extra) {
        super(start, end);
        this.extra = extra;
    }
}