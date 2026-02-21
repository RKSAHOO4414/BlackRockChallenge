package com.blackRock.repository;

import com.blackRock.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(t.finalRemanent) FROM Transaction t WHERE t.timestamp BETWEEN :start AND :end")
    Double sumRemanentInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    boolean existsByTimestamp(LocalDateTime timestamp);
}