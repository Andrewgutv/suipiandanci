package com.fragmentwords.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("device_preference")
public class DevicePreference {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceId;
    private Long vocabId;
    private Integer dailyGoal;
    private Boolean notificationEnabled;
    private Boolean soundEnabled;
    private Date createTime;
    private Date updateTime;
}
