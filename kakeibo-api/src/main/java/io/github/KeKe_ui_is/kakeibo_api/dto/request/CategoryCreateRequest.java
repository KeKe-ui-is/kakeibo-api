package io.github.KeKe_ui_is.kakeibo_api.dto.request;

import io.github.KeKe_ui_is.kakeibo_api.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryCreateRequest {

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotNull
    private TransactionType transactionType;

    @NotNull
    @PositiveOrZero
    private Integer displayOrder;
}