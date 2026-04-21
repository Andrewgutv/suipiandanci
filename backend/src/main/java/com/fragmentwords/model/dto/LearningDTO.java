package com.fragmentwords.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LearningDTO {
    @NotNull(message = "wordId is required")
    private Long wordId;

    @NotNull(message = "isKnown is required")
    private Boolean isKnown;
}
