package com.fragmentwords.model.dto;

import lombok.Data;

/**
 * 学习反馈DTO
 */
@Data
public class LearningDTO {
    private Long wordId;        // 单词ID
    private Boolean isKnown;    // 是否认识
}
