package com.fragmentwords.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fragmentwords.common.Result;
import com.fragmentwords.model.entity.Word;
import com.fragmentwords.service.UnknownWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/unknown")
public class UnknownWordController {

    @Autowired
    private UnknownWordService unknownWordService;

    //生词本列表
    @GetMapping("/list")
    public Result<Page<Word>> getUnknownWords(
            @RequestParam String deviceId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(unknownWordService.getUnknownWords(deviceId, pageNum, pageSize));
    }

    //添加单词到生词本
    @PostMapping("/add")
    public Result<Void> addUnknownWord(@RequestParam String deviceId, @RequestParam Long wordId) {
        unknownWordService.addUnknownWord(deviceId, wordId);
        return Result.success();
    }

    //从生词本移除单词
    @DeleteMapping("/remove")
    public Result<Void> removeUnknownWord(@RequestParam String deviceId, @RequestParam Long wordId) {
        unknownWordService.removeUnknownWord(deviceId, wordId);
        return Result.success();
    }

    //获取生词总数（App 首页展示 “生词本 X 个” 的数据源）
    @GetMapping("/count")
    public Result<Integer> getUnknownCount(@RequestParam String deviceId) {
        return Result.success(unknownWordService.getUnknownCount(deviceId));
    }
}