package com.fragmentwords.service;

import com.fragmentwords.model.dto.LearningDTO;
import com.fragmentwords.model.dto.LearningResponseDTO;
import com.fragmentwords.model.dto.NextWordDTO;
import com.fragmentwords.model.dto.ProgressStatsDTO;

/**
 * 学习进度Service
 */
public interface LearningProgressService {

    /**
     * 获取下一个需要学习的单词
     * @param deviceId 设备ID
     * @param userId 用户ID（可选）
     * @param request 请求参数（词库列表）
     * @return 下一个单词的学习响应
     */
    LearningResponseDTO getNextWord(String deviceId, Long userId, NextWordDTO request);

    /**
     * 处理用户学习反馈
     * @param deviceId 设备ID
     * @param userId 用户ID（可选）
     * @param feedback 学习反馈
     * @return 学习响应
     */
    LearningResponseDTO handleFeedback(String deviceId, Long userId, LearningDTO feedback);

    /**
     * 获取学习进度统计
     * @param deviceId 设备ID
     * @param userId 用户ID（可选）
     * @return 学习进度统计
     */
    ProgressStatsDTO getProgressStats(String deviceId, Long userId);

    /**
     * 获取单词学习详情
     * @param deviceId 设备ID
     * @param userId 用户ID（可选）
     * @param wordId 单词ID
     * @return 学习响应
     */
    LearningResponseDTO getWordProgress(String deviceId, Long userId, Long wordId);
}
