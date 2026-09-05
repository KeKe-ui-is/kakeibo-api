package io.github.KeKe_ui_is.kakeibo_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateRequest {

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotNull
    @PositiveOrZero
    private Integer displayOrder;
}