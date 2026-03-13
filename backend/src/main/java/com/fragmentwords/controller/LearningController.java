package com.fragmentwords.controller;

import com.fragmentwords.common.Result;
import com.fragmentwords.model.dto.LearningDTO;
import com.fragmentwords.model.dto.LearningResponseDTO;
import com.fragmentwords.model.dto.NextWordDTO;
import com.fragmentwords.model.dto.ProgressStatsDTO;
import com.fragmentwords.service.LearningProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 学习进度Controller
 */
@Tag(name = "学习进度管理", description = "艾宾浩斯遗忘曲线算法相关接口")
@RestController
@RequestMapping("/api/learning")
public class LearningController {

    @Autowired
    private LearningProgressService learningProgressService;

    @Operation(summary = "获取下一个单词", description = "根据艾宾浩斯算法智能推荐下一个需要学习的单词")
    @PostMapping("/next")
    public Result<LearningResponseDTO> getNextWord(
        @RequestHeader(value = "X-Device-ID", required = false) String deviceId,
        @RequestHeader(value = "X-User-ID", required = false) Long userId,
        @RequestBody(required = false) NextWordDTO request
    ) {
        if (request == null) {
            request = new NextWordDTO();
        }

        LearningResponseDTO response = learningProgressService.getNextWord(deviceId, userId, request);

        if (response == null) {
            return Result.error("没有可学习的单词");
        }

        return Result.success(response);
    }

    @Operation(summary = "提交学习反馈", description = "记录用户对单词的认识情况，自动计算下次复习时间")
    @PostMapping("/feedback")
    public Result<LearningResponseDTO> handleFeedback(
        @RequestHeader(value = "X-Device-ID", required = false) String deviceId,
        @RequestHeader(value = "X-User-ID", required = false) Long userId,
        @RequestBody LearningDTO feedback
    ) {
        LearningResponseDTO response = learningProgressService.handleFeedback(deviceId, userId, feedback);
        return Result.success(response);
    }

    @Operation(summary = "获取学习统计", description = "获取学习进度统计数据")
    @GetMapping("/stats")
    public Result<ProgressStatsDTO> getProgressStats(
        @RequestHeader(value = "X-Device-ID", required = false) String deviceId,
        @RequestHeader(value = "X-User-ID", required = false) Long userId
    ) {
        ProgressStatsDTO stats = learningProgressService.getProgressStats(deviceId, userId);
        return Result.success(stats);
    }

    @Operation(summary = "获取单词学习详情", description = "查询单个单词的学习进度")
    @GetMapping("/word/{wordId}")
    public Result<LearningResponseDTO> getWordProgress(
        @Parameter(description = "单词ID") @PathVariable Long wordId,
        @RequestHeader(value = "X-Device-ID", required = false) String deviceId,
        @RequestHeader(value = "X-User-ID", required = false) Long userId
    ) {
        LearningResponseDTO response = learningProgressService.getWordProgress(deviceId, userId, wordId);

        if (response == null) {
            return Result.error("单词不存在");
        }

        return Result.success(response);
    }
}
