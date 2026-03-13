package com.fragmentwords.model.dto;

import lombok.Data;
import java.util.Date;

/**
 * 学习响应DTO
 */
@Data
public class LearningResponseDTO {
    private Long wordId;
    private String word;
    private String phonetic;
    private String translation;
    private String example;
    private Integer stage;
    private String stageDescription;
    private Date nextReviewTime;
    private String timeUntilReview;
    private Integer retentionRate;
    private String studyAdvice;
    private Boolean isMastered;
}
