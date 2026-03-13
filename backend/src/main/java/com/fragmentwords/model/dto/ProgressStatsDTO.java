package com.fragmentwords.model.dto;

import lombok.Data;

/**
 * 学习进度统计DTO
 */
@Data
public class ProgressStatsDTO {
    private Integer totalWords;      // 总单词数
    private Integer masteredWords;   // 已掌握单词数
    private Integer inReviewWords;   // 复习中单词数
    private Integer needReviewWords; // 待复习单词数
    private Integer newWords;        // 新单词数
    private Integer avgRetentionRate; // 平均记忆保持率
    private Integer masteryRate;     // 掌握率（百分比）
}
