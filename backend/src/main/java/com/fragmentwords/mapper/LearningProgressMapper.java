package com.fragmentwords.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fragmentwords.model.entity.LearningProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学习进度Mapper
 */
@Mapper
public interface LearningProgressMapper extends BaseMapper<LearningProgress> {

    /**
     * 查询需要复习的单词（按复习时间排序）
     * @param deviceId 设备ID
     * @param userId 用户ID（可选）
     * @param vocabIds 词库ID列表（可选）
     * @param limit 限制数量
     * @return 需要复习的学习进度列表
     */
    @Select("<script>" +
            "SELECT lp.* FROM learning_progress lp " +
            "INNER JOIN word w ON lp.word_id = w.id " +
            "WHERE lp.is_mastered = false " +
            "AND lp.next_review_time &lt;= NOW() " +
            "AND (lp.device_id = #{deviceId} " +
            "<if test='userId != null'>" +
            "OR lp.user_id = #{userId}" +
            "</if>" +
            ") " +
            "<if test='vocabIds != null and vocabIds.size() > 0'>" +
            "AND w.vocab_id IN " +
            "<foreach item='id' collection='vocabIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</if>" +
            "ORDER BY lp.next_review_time ASC " +
            "LIMIT #{limit}" +
            "</script>")
    List<LearningProgress> findWordsToReview(
        @Param("deviceId") String deviceId,
        @Param("userId") Long userId,
        @Param("vocabIds") List<Long> vocabIds,
        @Param("limit") int limit
    );

    /**
     * 查询随机新单词（未学习过的）
     * @param deviceId 设备ID
     * @param userId 用户ID（可选）
     * @param vocabIds 词库ID列表（可选）
     * @param limit 限制数量
     * @return 随机单词ID列表
     */
    @Select("<script>" +
            "SELECT w.id FROM word w " +
            "WHERE w.id NOT IN (" +
            "  SELECT lp.word_id FROM learning_progress lp " +
            "  WHERE lp.device_id = #{deviceId} " +
            "<if test='userId != null'>" +
            "  OR lp.user_id = #{userId}" +
            "</if>" +
            ") " +
            "<if test='vocabIds != null and vocabIds.size() > 0'>" +
            "AND w.vocab_id IN " +
            "<foreach item='id' collection='vocabIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</if>" +
            "ORDER BY RAND() " +
            "LIMIT #{limit}" +
            "</script>")
    List<Long> findRandomNewWords(
        @Param("deviceId") String deviceId,
        @Param("userId") Long userId,
        @Param("vocabIds") List<Long> vocabIds,
        @Param("limit") int limit
    );

    /**
     * 统计学习进度
     * @param deviceId 设备ID
     * @param userId 用户ID（可选）
     * @return 统计结果 [总单词数, 已掌握数, 待复习数, 新单词数]
     */
    @Select("<script>" +
            "SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN lp.is_mastered = true THEN 1 ELSE 0 END) as mastered, " +
            "SUM(CASE WHEN lp.is_mastered = false AND lp.next_review_time &lt;= NOW() THEN 1 ELSE 0 END) as needReview, " +
            "SUM(CASE WHEN lp.stage = 0 THEN 1 ELSE 0 END) as newWords " +
            "FROM learning_progress lp " +
            "WHERE lp.device_id = #{deviceId} " +
            "<if test='userId != null'>" +
            "OR lp.user_id = #{userId}" +
            "</if>" +
            "</script>")
    LearningProgressStats getStats(
        @Param("deviceId") String deviceId,
        @Param("userId") Long userId
    );

    /**
     * 学习进度统计内部类
     */
    class LearningProgressStats {
        private Long total;
        private Long mastered;
        private Long needReview;
        private Long newWords;

        public Long getTotal() { return total; }
        public void setTotal(Long total) { this.total = total; }
        public Long getMastered() { return mastered; }
        public void setMastered(Long mastered) { this.mastered = mastered; }
        public Long getNeedReview() { return needReview; }
        public void setNeedReview(Long needReview) { this.needReview = needReview; }
        public Long getNewWords() { return newWords; }
        public void setNewWords(Long newWords) { this.newWords = newWords; }
    }
}
