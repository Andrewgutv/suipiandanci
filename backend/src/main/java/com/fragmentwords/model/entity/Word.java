package com.fragmentwords.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

//单词实体
@Data
@TableName("word")
public class Word {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String word;         // 单词
    private String phonetic;      // 音标
    private String translation;   // 翻译
    private String example;       // 例句
    private Long vocabId;         // 所属词库ID
    private Date createTime;
    private Date updateTime;
}
