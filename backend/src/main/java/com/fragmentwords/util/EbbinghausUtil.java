package com.fragmentwords.util;

import java.util.Arrays;
import java.util.List;

/**
 * 艾宾浩斯遗忘曲线算法工具类
 *
 * 核心原理：
 * 1. 8个复习节点：5分钟、30分钟、12小时、1天、2天、4天、7天、15天
 * 2. 用户点击"认识" → 进入下一个复习节点
 * 3. 用户点击"不认识" → 重置回第1个节点
 * 4. 完成所有8个节点 → 单词已掌握
 */
public class EbbinghausUtil {

    /**
     * 8个复习节点（单位：毫秒）
     */
    private static final List<Long> REVIEW_INTERVALS = Arrays.asList(
        5L * 60 * 1000,              // 5分钟
        30L * 60 * 1000,             // 30分钟
        12L * 60 * 60 * 1000,        // 12小时
        1L * 24 * 60 * 60 * 1000,    // 1天
        2L * 24 * 60 * 60 * 1000,    // 2天
        4L * 24 * 60 * 60 * 1000,    // 4天
        7L * 24 * 60 * 60 * 1000,    // 7天
        15L * 24 * 60 * 60 * 1000    // 15天
    );

    /**
     * 复习节点描述
     */
    public static final List<String> REVIEW_STAGE_DESCRIPTIONS = Arrays.asList(
        "5分钟后复习",
        "30分钟后复习",
        "12小时后复习",
        "1天后复习",
        "2天后复习",
        "4天后复习",
        "7天后复习",
        "15天后复习"
    );

    /**
     * 最大复习阶段（8表示已掌握）
     */
    public static final int MASTERED_STAGE = 8;

    /**
     * 计算下次复习时间
     * @param currentStage 当前复习阶段（0-7，0表示第一次学习）
     * @param isKnown 用户是否认识该单词
     * @return 下次复习时间戳（毫秒）
     */
    public static long calculateNextReviewTime(int currentStage, boolean isKnown) {
        long now = System.currentTimeMillis();

        if (isKnown) {
            // 认识：进入下一个复习阶段
            if (currentStage < REVIEW_INTERVALS.size()) {
                return now + REVIEW_INTERVALS.get(currentStage);
            } else {
                // 已完成所有复习阶段，设置为很久以后（表示已掌握）
                return now + 365L * 24 * 60 * 60 * 1000;
            }
        } else {
            // 不认识：重置回第一个复习阶段（5分钟后）
            return now + REVIEW_INTERVALS.get(0);
        }
    }

    /**
     * 计算下次复习阶段
     * @param currentStage 当前复习阶段
     * @param isKnown 用户是否认识该单词
     * @return 下次复习阶段（0-8，8表示已掌握）
     */
    public static int calculateNextStage(int currentStage, boolean isKnown) {
        if (isKnown) {
            return Math.min(currentStage + 1, MASTERED_STAGE);
        } else {
            return 0; // 重置回0
        }
    }

    /**
     * 检查单词是否需要复习
     * @param nextReviewTime 下次复习时间戳
     * @return 是否需要复习
     */
    public static boolean needsReview(long nextReviewTime) {
        return System.currentTimeMillis() >= nextReviewTime;
    }

    /**
     * 获取复习阶段的描述
     * @param stage 复习阶段（0-8）
     * @return 阶段描述
     */
    public static String getStageDescription(int stage) {
        if (stage <= 7) {
            return "第" + (stage + 1) + "次复习：" + REVIEW_STAGE_DESCRIPTIONS.get(stage);
        } else {
            return "已掌握";
        }
    }

    /**
     * 获取距离下次复习的时间描述
     * @param nextReviewTime 下次复习时间戳
     * @return 时间描述（如"2小时后"、"3天后"）
     */
    public static String getTimeUntilReview(long nextReviewTime) {
        long now = System.currentTimeMillis();
        long diff = nextReviewTime - now;

        if (diff <= 0) return "现在";

        long minutes = diff / (1000 * 60);
        long hours = diff / (1000 * 60 * 60);
        long days = diff / (1000 * 60 * 60 * 24);

        if (minutes < 60) {
            return minutes + "分钟后";
        } else if (hours < 24) {
            return hours + "小时后";
        } else if (days < 30) {
            return days + "天后";
        } else {
            return (days / 30) + "个月后";
        }
    }

    /**
     * 计算记忆保持率（基于艾宾浩斯遗忘曲线）
     * @param stage 当前复习阶段
     * @return 记忆保持率（0-100）
     */
    public static int calculateRetentionRate(int stage) {
        // 艾宾浩斯遗忘曲线：每次复习后记忆保持率提升
        int[] baseRates = {20, 58, 72, 80, 85, 88, 90, 92, 95};
        if (stage >= 0 && stage < baseRates.length) {
            return baseRates[stage];
        }
        return 95;
    }

    /**
     * 获取学习建议
     * @param stage 当前复习阶段
     * @param isKnown 用户是否认识
     * @return 学习建议
     */
    public static String getStudyAdvice(int stage, boolean isKnown) {
        if (isKnown) {
            if (stage == 0) {
                return "很好！5分钟后复习一次";
            } else if (stage == 1) {
                return "不错！12小时后复习";
            } else if (stage == 2) {
                return "坚持！明天复习";
            } else if (stage <= 4) {
                return "继续加油！保持复习节奏";
            } else if (stage <= 7) {
                return "即将掌握！坚持最后几次复习";
            } else {
                return "恭喜！这个单词已经掌握";
            }
        } else {
            return "没关系，5分钟后会再次出现，加强记忆";
        }
    }

    /**
     * 检查是否已掌握
     * @param stage 复习阶段
     * @return 是否已掌握
     */
    public static boolean isMastered(int stage) {
        return stage >= MASTERED_STAGE;
    }
}
