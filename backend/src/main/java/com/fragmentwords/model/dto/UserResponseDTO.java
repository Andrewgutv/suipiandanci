package com.fragmentwords.model.dto;

import lombok.Data;

/**
 * 用户信息响应DTO
 */
@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String phone;
    private String deviceId;
}
