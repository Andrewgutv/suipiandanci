package com.fragmentwords.model.dto;

import lombok.Data;

/**
 * 用户登录响应DTO
 */
@Data
public class UserLoginResponseDTO {
    private Long userId;
    private String username;
    private String token;
    private String phone;
}
