package com.fragmentwords.service;

import com.fragmentwords.model.entity.Vocab;
import java.util.List;

public interface VocabService {
    // 获取所有词库列表
    List<Vocab> getAllVocabs();
    // 获取当前设备选中的词库
    Vocab getSelectedVocab(String deviceId);
    // 切换词库
    void selectVocab(String deviceId, Long vocabId);
}