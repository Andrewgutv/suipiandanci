package com.fragmentwords.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 学习进度实体（艾宾浩斯遗忘曲线算法核心）
 */
@Data
@TableName("learning_progress")
public class LearningProgress {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceId;        // 设备ID
    private Long userId;            // 用户ID（登录后关联）
    private Long wordId;            // 单词ID
    private Integer stage;          // 艾宾浩斯阶段（0-8）
    private Integer knownCount;     // 认识次数
    private Integer unknownCount;   // 不认识次数
    private Date nextReviewTime;    // 下次复习时间
    private Date lastReviewTime;    // 最后复习时间
    private Boolean isMastered;     // 是否已掌握
    private Date createTime;
    private Date updateTime;
}
