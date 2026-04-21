package com.fragmentwords.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fragmentwords.common.ResourceNotFoundException;
import com.fragmentwords.mapper.LearningProgressMapper;
import com.fragmentwords.mapper.WordMapper;
import com.fragmentwords.model.dto.LearningDTO;
import com.fragmentwords.model.dto.LearningResponseDTO;
import com.fragmentwords.model.dto.NextWordDTO;
import com.fragmentwords.model.dto.ProgressStatsDTO;
import com.fragmentwords.model.entity.LearningProgress;
import com.fragmentwords.model.entity.Word;
import com.fragmentwords.service.LearningProgressService;
import com.fragmentwords.util.EbbinghausUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 学习进度Service实现
 */
@Service
public class LearningProgressServiceImpl implements LearningProgressService {

    @Autowired
    private LearningProgressMapper learningProgressMapper;

    @Autowired
    private WordMapper wordMapper;

    @Override
    public LearningResponseDTO getNextWord(String deviceId, Long userId, NextWordDTO request) {
        // 1. 优先获取需要复习的单词
        List<LearningProgress> reviewList = learningProgressMapper.findWordsToReview(
            deviceId, userId, request.getVocabIds(), 1
        );

        if (!reviewList.isEmpty()) {
            LearningProgress progress = reviewList.get(0);
            Word word = getWordOrThrow(progress.getWordId());
            return buildResponse(word, progress);
        }

        // 2. 获取随机新单词
        List<Long> newWordIds = learningProgressMapper.findRandomNewWords(
            deviceId, userId, request.getVocabIds(), 1
        );

        if (!newWordIds.isEmpty()) {
            Long wordId = newWordIds.get(0);
            Word word = getWordOrThrow(wordId);

            // 初始化学习进度
            LearningProgress progress = new LearningProgress();
            progress.setDeviceId(deviceId);
            progress.setUserId(userId);
            progress.setWordId(wordId);
            progress.setStage(0);
            progress.setKnownCount(0);
            progress.setUnknownCount(0);
            progress.setNextReviewTime(new Date());
            progress.setLastReviewTime(new Date(0));
            progress.setIsMastered(false);
            progress.setCreateTime(new Date());
            progress.setUpdateTime(new Date());

            learningProgressMapper.insert(progress);

            return buildResponse(word, progress);
        }

        // 3. 没有新单词，返回随机已学单词
        LambdaQueryWrapper<LearningProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningProgress::getDeviceId, deviceId);
        if (userId != null) {
            wrapper.or().eq(LearningProgress::getUserId, userId);
        }
        wrapper.eq(LearningProgress::getIsMastered, false);
        wrapper.orderByAsc(LearningProgress::getNextReviewTime);
        wrapper.last("LIMIT 1");

        LearningProgress progress = learningProgressMapper.selectOne(wrapper);
        if (progress != null) {
            Word word = getWordOrThrow(progress.getWordId());
            return buildResponse(word, progress);
        }

        throw new ResourceNotFoundException("No learnable word is currently available");
    }

    @Override
    @Transactional
    public LearningResponseDTO handleFeedback(String deviceId, Long userId, LearningDTO feedback) {
        Word word = getWordOrThrow(feedback.getWordId());
        // 1. 查询现有学习进度
        LambdaQueryWrapper<LearningProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningProgress::getDeviceId, deviceId);
        wrapper.eq(LearningProgress::getWordId, feedback.getWordId());
        if (userId != null) {
            wrapper.or().eq(LearningProgress::getUserId, userId);
        }

        LearningProgress progress = learningProgressMapper.selectOne(wrapper);

        if (progress == null) {
            // 如果不存在，创建新记录
            progress = new LearningProgress();
            progress.setDeviceId(deviceId);
            progress.setUserId(userId);
            progress.setWordId(feedback.getWordId());
            progress.setStage(0);
            progress.setKnownCount(0);
            progress.setUnknownCount(0);
        }

        // 2. 应用艾宾浩斯算法
        boolean isKnown = feedback.getIsKnown();
        int currentStage = progress.getStage() != null ? progress.getStage() : 0;
        int nextStage = EbbinghausUtil.calculateNextStage(currentStage, isKnown);
        long nextReviewTimeMs = EbbinghausUtil.calculateNextReviewTime(currentStage, isKnown);

        // 3. 更新学习进度
        progress.setStage(nextStage);
        progress.setNextReviewTime(new Date(nextReviewTimeMs));
        progress.setLastReviewTime(new Date());
        progress.setIsMastered(EbbinghausUtil.isMastered(nextStage));
        progress.setUpdateTime(new Date());

        if (isKnown) {
            progress.setKnownCount((progress.getKnownCount() != null ? progress.getKnownCount() : 0) + 1);
        } else {
            progress.setUnknownCount((progress.getUnknownCount() != null ? progress.getUnknownCount() : 0) + 1);
        }

        // 4. 保存到数据库
        if (progress.getId() == null) {
            progress.setCreateTime(new Date());
            learningProgressMapper.insert(progress);
        } else {
            learningProgressMapper.updateById(progress);
        }

        // 5. 返回响应
        return buildResponse(word, progress);
    }

    @Override
    public ProgressStatsDTO getProgressStats(String deviceId, Long userId) {
        LearningProgressMapper.LearningProgressStats stats =
            learningProgressMapper.getStats(deviceId, userId);

        ProgressStatsDTO dto = new ProgressStatsDTO();

        if (stats != null) {
            long total = stats.getTotal() != null ? stats.getTotal() : 0;
            long mastered = stats.getMastered() != null ? stats.getMastered() : 0;
            long needReview = stats.getNeedReview() != null ? stats.getNeedReview() : 0;
            long newWords = stats.getNewWords() != null ? stats.getNewWords() : 0;

            dto.setTotalWords((int) total);
            dto.setMasteredWords((int) mastered);
            dto.setInReviewWords((int) (total - mastered - newWords));
            dto.setNeedReviewWords((int) needReview);
            dto.setNewWords((int) newWords);
            dto.setMasteryRate(total > 0 ? (int) (mastered * 100 / total) : 0);
            dto.setAvgRetentionRate(calculateAvgRetentionRate(deviceId, userId));
        }

        return dto;
    }

    @Override
    public LearningResponseDTO getWordProgress(String deviceId, Long userId, Long wordId) {
        LambdaQueryWrapper<LearningProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningProgress::getDeviceId, deviceId);
        wrapper.eq(LearningProgress::getWordId, wordId);
        if (userId != null) {
            wrapper.or().eq(LearningProgress::getUserId, userId);
        }

        LearningProgress progress = learningProgressMapper.selectOne(wrapper);
        Word word = getWordOrThrow(wordId);

        if (progress == null) {
            // 创建默认进度
            progress = new LearningProgress();
            progress.setStage(0);
            progress.setNextReviewTime(new Date());
            progress.setIsMastered(false);
        }

        return buildResponse(word, progress);
    }

    /**
     * 构建学习响应DTO
     */
    private LearningResponseDTO buildResponse(Word word, LearningProgress progress) {
        LearningResponseDTO dto = new LearningResponseDTO();
        dto.setWordId(word.getId());
        dto.setWord(word.getWord());
        dto.setPhonetic(word.getPhonetic());
        dto.setTranslation(word.getTranslation());
        dto.setExample(word.getExample());

        if (progress != null) {
            int stage = progress.getStage() != null ? progress.getStage() : 0;
            dto.setStage(stage);
            dto.setStageDescription(EbbinghausUtil.getStageDescription(stage));

            Date nextReviewTime = progress.getNextReviewTime();
            if (nextReviewTime != null) {
                dto.setNextReviewTime(nextReviewTime);
                dto.setTimeUntilReview(EbbinghausUtil.getTimeUntilReview(nextReviewTime.getTime()));
            }

            dto.setRetentionRate(EbbinghausUtil.calculateRetentionRate(stage));
            dto.setIsMastered(progress.getIsMastered() != null ? progress.getIsMastered() : false);
        }

        return dto;
    }

    /**
     * 计算平均记忆保持率
     */
    private Word getWordOrThrow(Long wordId) {
        Word word = wordMapper.selectById(wordId);
        if (word == null) {
            throw new ResourceNotFoundException("Word not found: " + wordId);
        }
        return word;
    }

    private int calculateAvgRetentionRate(String deviceId, Long userId) {
        LambdaQueryWrapper<LearningProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningProgress::getDeviceId, deviceId);
        if (userId != null) {
            wrapper.or().eq(LearningProgress::getUserId, userId);
        }

        List<LearningProgress> allProgress = learningProgressMapper.selectList(wrapper);

        if (allProgress.isEmpty()) {
            return 0;
        }

        int totalRate = 0;
        for (LearningProgress progress : allProgress) {
            int stage = progress.getStage() != null ? progress.getStage() : 0;
            totalRate += EbbinghausUtil.calculateRetentionRate(stage);
        }

        return totalRate / allProgress.size();
    }
}
