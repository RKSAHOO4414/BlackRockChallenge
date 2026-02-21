package com.blackRock.dto;

import com.blackRock.model.Expense;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParseRequest {

    @Valid
    @NotEmpty(message = "Expenses list cannot be empty")
    private List<Expense> expenses;
}