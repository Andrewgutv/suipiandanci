package com.fragmentwords.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

//词库实体
@Data
@TableName("vocab")
public class Vocab {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;          // 词库名称（如：四级、六级、考研）
    private Integer wordCount;    // 单词总数
    private String description;   // 描述
    private Date createTime;
}