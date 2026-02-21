package com.blackRock.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Double ceiling;

    @Column(nullable = false)
    private Double remanent;

    @Column(nullable = false)
    private Double finalRemanent; // After q and p rules

    public Transaction(LocalDateTime timestamp, Double amount, Double ceiling, Double remanent) {
        this.timestamp = timestamp;
        this.amount = amount;
        this.ceiling = ceiling;
        this.remanent = remanent;
        this.finalRemanent = remanent;
    }
}