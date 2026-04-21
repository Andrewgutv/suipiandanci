package com.fragmentwords.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fragmentwords.mapper.UnknownWordMapper;
import com.fragmentwords.mapper.VocabMapper;
import com.fragmentwords.mapper.WordMapper;
import com.fragmentwords.model.dto.NotebookItemDTO;
import com.fragmentwords.model.dto.NotebookPageDTO;
import com.fragmentwords.model.entity.UnknownWord;
import com.fragmentwords.model.entity.Vocab;
import com.fragmentwords.model.entity.Word;
import com.fragmentwords.service.UnknownWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UnknownWordServiceImpl extends ServiceImpl<UnknownWordMapper, UnknownWord> implements UnknownWordService {

    @Autowired
    private WordMapper wordMapper;

    @Autowired
    private VocabMapper vocabMapper;

    @Override
    public NotebookPageDTO getUnknownWords(String deviceId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<UnknownWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UnknownWord::getDeviceId, deviceId)
                .orderByDesc(UnknownWord::getId);

        Page<UnknownWord> unknownPage = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<UnknownWord> unknownWords = unknownPage.getRecords();
        if (unknownWords.isEmpty()) {
            return new NotebookPageDTO(pageNum, pageSize, unknownPage.getTotal(), Collections.emptyList());
        }

        List<Long> wordIds = unknownWords.stream()
                .map(UnknownWord::getWordId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, Word> wordsById = wordMapper.selectBatchIds(wordIds).stream()
                .collect(Collectors.toMap(Word::getId, word -> word, (left, right) -> left, LinkedHashMap::new));

        List<Long> vocabIds = wordsById.values().stream()
                .map(Word::getVocabId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> vocabNamesById = vocabIds.isEmpty()
                ? Collections.emptyMap()
                : vocabMapper.selectBatchIds(vocabIds).stream()
                .collect(Collectors.toMap(Vocab::getId, Vocab::getName, (left, right) -> left));

        List<NotebookItemDTO> items = unknownWords.stream()
                .map(unknownWord -> toNotebookItem(unknownWord, wordsById, vocabNamesById))
                .filter(Objects::nonNull)
                .toList();

        return new NotebookPageDTO(pageNum, pageSize, unknownPage.getTotal(), items);
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

    private NotebookItemDTO toNotebookItem(
            UnknownWord unknownWord,
            Map<Long, Word> wordsById,
            Map<Long, String> vocabNamesById) {
        Word word = wordsById.get(unknownWord.getWordId());
        if (word == null) {
            return null;
        }

        NotebookItemDTO item = new NotebookItemDTO();
        item.setWordId(word.getId());
        item.setWord(word.getWord());
        item.setPhonetic(word.getPhonetic());
        item.setTranslation(word.getTranslation());
        item.setExample(word.getExample());
        item.setVocabId(word.getVocabId());
        item.setVocabName(vocabNamesById.get(word.getVocabId()));
        item.setAddedAt(unknownWord.getCreateTime());
        return item;
    }
}
