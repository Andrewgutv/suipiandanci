package com.fragmentwords.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fragmentwords.mapper.WordMapper;
import com.fragmentwords.model.dto.WordQueryDTO;
import com.fragmentwords.model.entity.Word;
import com.fragmentwords.service.WordService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.Random;

@Service
public class WordServiceImpl extends ServiceImpl<WordMapper, Word> implements WordService {

    @Override
    public Page<Word> getPage(WordQueryDTO queryDTO, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Word> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getVocabId() != null, Word::getVocabId, queryDTO.getVocabId());
        wrapper.like(StringUtils.hasText(queryDTO.getWord()), Word::getWord, queryDTO.getWord());
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Word getRandomWord(Long vocabId) {
        LambdaQueryWrapper<Word> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Word::getVocabId, vocabId);
        long count = this.count(wrapper);
        if (count == 0) return null;
        int randomIndex = new Random().nextInt((int) count);
        Page<Word> page = new Page<>(randomIndex + 1, 1);
        Page<Word> result = this.page(page, wrapper);
        return result.getRecords().isEmpty() ? null : result.getRecords().get(0);
    }

    @Override
    public Word refreshWord(Long vocabId) {
        return getRandomWord(vocabId);
    }
}
