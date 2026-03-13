package com.fragmentwords.model.dto;

import lombok.Data;

@Data
public class VocabSelectDTO {
    private String deviceId;      // 设备ID
    private Long vocabId;         // 选中的词库ID
}
