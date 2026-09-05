package io.github.KeKe_ui_is.kakeibo_api.dto.response;

import io.github.KeKe_ui_is.kakeibo_api.model.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CategoryResponse {

    private Long id;
    private Long userId;
    private String name;
    private TransactionType transactionType;
    private Integer displayOrder;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}