package com.fragmentwords.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 获取下一个单词请求DTO
 */
@Data
public class NextWordDTO {
    private List<Long> vocabIds; // 词库ID列表（可选，为空则从所有词库获取）
    private Long excludeWordId;  // 需要排除的单词ID（可选，避免返回刚看过的单词）
}
