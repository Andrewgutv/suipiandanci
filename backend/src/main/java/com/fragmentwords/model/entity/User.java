package com.fragmentwords.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 用户实体
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;       // 用户名
    private String password;       // 密码（加密）
    private String phone;          // 手机号
    private String deviceId;       // 设备ID（用于未登录用户）
    private Date createTime;
    private Date updateTime;
}
