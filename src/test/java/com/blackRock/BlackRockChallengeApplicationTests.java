package com.blackRock;

import com.blackRock.dto.ParseResponse;
import com.blackRock.model.Expense;
import com.blackRock.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BlackRockChallengeApplicationTests {

	@Autowired
	private TransactionService transactionService;

	@Test
	public void testParseExpenses_ValidExpenses() {
		// Test Type: Unit Test
		// Validation: Verify correct ceiling and remanent calculation
		// Command: mvn test -Dtest=TransactionServiceTest#testParseExpenses_ValidExpenses

		List<Expense> expenses = Arrays.asList(
				new Expense(LocalDateTime.parse("2023-10-12T20:15:00"), 250.0),
				new Expense(LocalDateTime.parse("2023-02-28T15:49:00"), 375.0)
		);

		ParseResponse response = transactionService.parseExpenses(expenses);

		assertEquals(2, response.getValid().size());
		assertEquals(0, response.getInvalid().size());
		assertEquals(300.0, response.getValid().get(0).getCeiling());
		assertEquals(50.0, response.getValid().get(0).getRemanent());
		assertEquals(400.0, response.getValid().get(1).getCeiling());
		assertEquals(25.0, response.getValid().get(1).getRemanent());
	}

	@Test
	public void testParseExpenses_DuplicateTimestamp() {
		// Test Type: Unit Test
		// Validation: Duplicate timestamps should be rejected
		// Command: mvn test -Dtest=TransactionServiceTest#testParseExpenses_DuplicateTimestamp

		List<Expense> expenses = Arrays.asList(
				new Expense(LocalDateTime.parse("2023-10-12T20:15:00"), 250.0),
				new Expense(LocalDateTime.parse("2023-10-12T20:15:00"), 375.0)
		);

		ParseResponse response = transactionService.parseExpenses(expenses);

		assertEquals(1, response.getValid().size());
		assertEquals(1, response.getInvalid().size());
		assertEquals("Duplicate timestamp", response.getInvalid().get(0).getMessage());
	}

}
