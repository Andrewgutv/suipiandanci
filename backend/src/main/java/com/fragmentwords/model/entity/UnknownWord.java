package com.fragmentwords.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;


//生词本实体
@Data
@TableName("unknown_word")
public class UnknownWord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long wordId;          // 单词ID
    private String deviceId;      // 设备ID（替代用户ID，无登录场景）
    private Date createTime;      // 添加时间
}
