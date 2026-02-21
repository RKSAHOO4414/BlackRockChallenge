package com.blackRock.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KPeriod extends Period {

    public KPeriod(LocalDateTime start, LocalDateTime end) {
        super(start, end);
    }
}