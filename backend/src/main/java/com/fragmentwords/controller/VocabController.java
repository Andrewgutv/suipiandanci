package com.fragmentwords.controller;

import com.fragmentwords.common.Result;
import com.fragmentwords.model.dto.VocabSelectDTO;
import com.fragmentwords.model.dto.VocabSelectionResponseDTO;
import com.fragmentwords.model.entity.Vocab;
import com.fragmentwords.service.VocabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/vocabs", produces = "application/json;charset=UTF-8")
public class VocabController {

    @Autowired
    private VocabService vocabService;

    //词库列表(返回系统中所有可用的词库)
    @GetMapping
    public Result<List<Vocab>> getAllVocabs() {
        return Result.success(vocabService.getAllVocabs());
    }

    //返回当前设备上次选中的词库（App 启动时自动加载用户偏好）
    @GetMapping("/current")
    public Result<VocabSelectionResponseDTO> getSelectedVocab(
            @RequestHeader("X-Device-Id") String deviceId) {
        Vocab vocab = vocabService.getSelectedVocab(deviceId);
        return Result.success(new VocabSelectionResponseDTO(vocab != null ? vocab.getId() : null));
    }

    //保存当前设备选中的词库（App 点击 “切换词库” 后调用）
    @PutMapping("/current")
    public Result<Void> selectVocab(
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestBody VocabSelectDTO request) {
        vocabService.selectVocab(deviceId, request.getVocabId());
        return Result.success();
    }
}
