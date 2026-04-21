package com.fragmentwords.controller;

import com.fragmentwords.common.Result;
import com.fragmentwords.config.JwtAuthInterceptor;
import com.fragmentwords.model.dto.LearningDTO;
import com.fragmentwords.model.dto.LearningResponseDTO;
import com.fragmentwords.model.dto.NextWordDTO;
import com.fragmentwords.model.dto.ProgressStatsDTO;
import com.fragmentwords.service.LearningProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Learning Progress", description = "Learning progress endpoints")
@RestController
@RequestMapping(value = "/api/v1/learning", produces = "application/json;charset=UTF-8")
public class LearningController {

    @Autowired
    private LearningProgressService learningProgressService;

    @Operation(summary = "Get next word", description = "Recommend the next word to learn")
    @PostMapping("/next")
    public Result<LearningResponseDTO> getNextWord(
        @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
        @RequestBody(required = false) NextWordDTO request,
        HttpServletRequest httpRequest
    ) {
        Long userId = resolveUserId(httpRequest);
        if (request == null) {
            request = new NextWordDTO();
        }
        return Result.success(learningProgressService.getNextWord(deviceId, userId, request));
    }

    @Operation(summary = "Submit feedback", description = "Record known or unknown feedback for a word")
    @PostMapping("/feedback")
    public Result<LearningResponseDTO> handleFeedback(
        @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
        @Valid @RequestBody LearningDTO feedback,
        HttpServletRequest httpRequest
    ) {
        Long userId = resolveUserId(httpRequest);
        return Result.success(learningProgressService.handleFeedback(deviceId, userId, feedback));
    }

    @Operation(summary = "Get stats", description = "Fetch learning progress statistics")
    @GetMapping("/stats")
    public Result<ProgressStatsDTO> getProgressStats(
        @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
        HttpServletRequest httpRequest
    ) {
        Long userId = resolveUserId(httpRequest);
        return Result.success(learningProgressService.getProgressStats(deviceId, userId));
    }

    @Operation(summary = "Get word progress", description = "Fetch progress for one word")
    @GetMapping("/word/{wordId}")
    public Result<LearningResponseDTO> getWordProgress(
        @Parameter(description = "Word ID") @PathVariable Long wordId,
        @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
        HttpServletRequest httpRequest
    ) {
        Long userId = resolveUserId(httpRequest);
        return Result.success(learningProgressService.getWordProgress(deviceId, userId, wordId));
    }

    private Long resolveUserId(HttpServletRequest request) {
        Object attribute = request.getAttribute(JwtAuthInterceptor.AUTHENTICATED_USER_ID);
        if (attribute instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
