package com.blackRock;

import com.blackRock.dto.ReturnsRequest;
import com.blackRock.dto.ReturnsResponse;
import com.blackRock.model.KPeriod;
import com.blackRock.model.PPeriod;
import com.blackRock.model.QPeriod;
import com.blackRock.model.Transaction;
import com.blackRock.service.ReturnsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReturnsServiceTest {

    @Autowired
    private ReturnsService returnsService;

    @Test
    public void testCalculateReturns_ExampleFromPDF() {
        // Test Type: Integration Test
        // Validation: Verify against the example provided in the challenge PDF
        // Command: mvn test -Dtest=ReturnsServiceTest#testCalculateReturns_ExampleFromPDF

        // Create transactions
        List<Transaction> transactions = Arrays.asList(
                new Transaction(LocalDateTime.parse("2023-10-12T20:15:00"), 250.0, 300.0, 50.0),
                new Transaction(LocalDateTime.parse("2023-02-28T15:49:00"), 375.0, 400.0, 25.0),
                new Transaction(LocalDateTime.parse("2023-07-01T21:59:00"), 620.0, 700.0, 80.0),
                new Transaction(LocalDateTime.parse("2023-12-17T08:09:00"), 480.0, 500.0, 20.0)
        );

        // Create q periods
        List<QPeriod> qPeriods = Arrays.asList(
                new QPeriod(
                        LocalDateTime.parse("2023-07-01T00:00:00"),
                        LocalDateTime.parse("2023-07-31T23:59:00"),
                        0.0
                )
        );

        // Create p periods
        List<PPeriod> pPeriods = Arrays.asList(
                new PPeriod(
                        LocalDateTime.parse("2023-10-01T08:00:00"),
                        LocalDateTime.parse("2023-12-31T19:59:00"),
                        25.0
                )
        );

        // Create k periods
        List<KPeriod> kPeriods = Arrays.asList(
                new KPeriod(
                        LocalDateTime.parse("2023-03-01T00:00:00"),
                        LocalDateTime.parse("2023-11-30T23:59:00")
                ),
                new KPeriod(
                        LocalDateTime.parse("2023-01-01T00:00:00"),
                        LocalDateTime.parse("2023-12-31T23:59:00")
                )
        );

        ReturnsRequest request = new ReturnsRequest(
                29, 50000.0, 5.5, qPeriods, pPeriods, kPeriods, transactions
        );

        ReturnsResponse response = returnsService.calculateNPSReturns(request);

        assertEquals("NPS", response.getInstrument());
        assertEquals(2, response.getResults().size());

        // Verify first period (March-November) should be 75
        assertEquals(75.0, response.getResults().get(0).getAmount(), 0.01);

        // Verify second period (full year) should be 145
        assertEquals(145.0, response.getResults().get(1).getAmount(), 0.01);
    }
}