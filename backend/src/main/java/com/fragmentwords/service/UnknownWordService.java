package com.fragmentwords.service;

import com.fragmentwords.model.dto.NotebookPageDTO;

public interface UnknownWordService {
    // 获取生词本列表（分页）
    NotebookPageDTO getUnknownWords(String deviceId, Integer pageNum, Integer pageSize);
    // 添加单词到生词本
    void addUnknownWord(String deviceId, Long wordId);
    // 从生词本移除单词
    void removeUnknownWord(String deviceId, Long wordId);
    // 获取生词总数
    int getUnknownCount(String deviceId);
}
