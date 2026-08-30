package io.github.KeKe_ui_is.kakeibo_api.dto.request;

import io.github.KeKe_ui_is.kakeibo_api.model.ExpenseType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class TransactionRequest {

    @NotNull
    private Long categoryId;

    @NotNull
    @Positive
    private Long amount;

    @NotNull
    private LocalDate transactionDate;

    private ExpenseType expenseType;

    @Size(max = 500)
    private String memo;
}