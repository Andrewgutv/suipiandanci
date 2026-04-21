package com.fragmentwords.controller;

import com.fragmentwords.common.Result;
import com.fragmentwords.model.entity.Word;
import com.fragmentwords.service.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/word", produces = "application/json;charset=UTF-8")
public class WordController {

    @Autowired
    private WordService wordService;

    //随机获取单词
    @GetMapping("/random")
    public Result<Word> getRandomWord(@RequestParam Long vocabId) {
        Word word = wordService.getRandomWord(vocabId);
        return Result.success(word);
    }

    //手动刷新单词
    @PostMapping("/refresh")
    public Result<Word> refreshWord(@RequestParam Long vocabId) {
        Word word = wordService.refreshWord(vocabId);
        return Result.success(word);
    }
}
