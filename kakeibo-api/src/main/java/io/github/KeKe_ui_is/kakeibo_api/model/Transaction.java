package io.github.KeKe_ui_is.kakeibo_api.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Transaction {

    private Long id;
    private Long userId;
    private Long categoryId;
    private Long amount;
    private LocalDate transactionDate;
    private ExpenseType expenseType;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // getter・setter
}