package com.fragmentwords.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 获取下一个单词请求DTO
 */
@Data
public class NextWordDTO {
    private List<Long> vocabIds; // 词库ID列表（可选，为空则从所有词库获取）
}
