package com.fragmentwords.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fragmentwords.mapper.UnknownWordMapper;
import com.fragmentwords.mapper.WordMapper;
import com.fragmentwords.model.entity.UnknownWord;
import com.fragmentwords.model.entity.Word;
import com.fragmentwords.service.UnknownWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UnknownWordServiceImpl extends ServiceImpl<UnknownWordMapper, UnknownWord> implements UnknownWordService {

    @Autowired
    private WordMapper wordMapper;

    @Override
    public Page<Word> getUnknownWords(String deviceId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<UnknownWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UnknownWord::getDeviceId, deviceId);
        Page<UnknownWord> unknownPage = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<Long> wordIds = unknownPage.getRecords().stream()
                .map(UnknownWord::getWordId)
                .collect(Collectors.toList());
        List<Word> words = wordMapper.selectBatchIds(wordIds);
        Page<Word> resultPage = new Page<>(pageNum, pageSize, unknownPage.getTotal());
        resultPage.setRecords(words);
        return resultPage;
    }

    @Override
    public void addUnknownWord(String deviceId, Long wordId) {
        LambdaQueryWrapper<UnknownWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UnknownWord::getDeviceId, deviceId)
                .eq(UnknownWord::getWordId, wordId);
        if (this.count(wrapper) > 0) return;
        UnknownWord unknownWord = new UnknownWord();
        unknownWord.setDeviceId(deviceId);
        unknownWord.setWordId(wordId);
        this.save(unknownWord);
    }

    @Override
    public void removeUnknownWord(String deviceId, Long wordId) {
        LambdaQueryWrapper<UnknownWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UnknownWord::getDeviceId, deviceId)
                .eq(UnknownWord::getWordId, wordId);
        this.remove(wrapper);
    }

    @Override
    public int getUnknownCount(String deviceId) {
        LambdaQueryWrapper<UnknownWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UnknownWord::getDeviceId, deviceId);
        return (int) this.count(wrapper);
    }
}