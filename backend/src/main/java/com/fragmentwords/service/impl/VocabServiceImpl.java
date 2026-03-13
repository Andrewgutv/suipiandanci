package com.fragmentwords.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fragmentwords.mapper.VocabMapper;
import com.fragmentwords.model.entity.Vocab;
import com.fragmentwords.service.VocabService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VocabServiceImpl extends ServiceImpl<VocabMapper, Vocab> implements VocabService {

    // 模拟设备-词库映射（实际可存入数据库表device_vocab）
    private String currentDeviceVocab = "";
    private Long currentVocabId = 1L; // 默认四级词库

    @Override
    public List<Vocab> getAllVocabs() {
        return this.list();
    }

    @Override
    public Vocab getSelectedVocab(String deviceId) {
        return this.getById(currentVocabId);
    }

    @Override
    public void selectVocab(String deviceId, Long vocabId) {
        currentDeviceVocab = deviceId;
        currentVocabId = vocabId;
    }
}