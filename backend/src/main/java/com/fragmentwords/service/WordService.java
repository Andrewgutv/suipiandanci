package com.fragmentwords.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fragmentwords.model.dto.WordQueryDTO;
import com.fragmentwords.model.entity.Word;

public interface WordService {
    // 分页查询单词
    Page<Word> getPage(WordQueryDTO queryDTO, Integer pageNum, Integer pageSize);
    // 从指定词库随机获取一个单词
    Word getRandomWord(Long vocabId);
    // 立即刷新（获取新随机单词）
    Word refreshWord(Long vocabId);
}