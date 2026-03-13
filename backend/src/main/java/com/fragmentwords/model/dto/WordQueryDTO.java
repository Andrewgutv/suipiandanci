package com.fragmentwords.model.dto;

import lombok.Data;

@Data
public class WordQueryDTO {
    private Long vocabId;         // 词库ID
    private String word;          // 模糊查询单词
}