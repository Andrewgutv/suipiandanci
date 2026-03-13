package com.fragmentwords.service;

import com.fragmentwords.model.dto.UserLoginDTO;
import com.fragmentwords.model.dto.UserLoginResponseDTO;
import com.fragmentwords.model.dto.UserRegisterDTO;
import com.fragmentwords.model.dto.UserResponseDTO;

/**
 * 用户Service
 */
public interface UserService {

    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 用户信息
     */
    UserResponseDTO register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录响应（包含token）
     */
    UserLoginResponseDTO login(UserLoginDTO loginDTO);

    /**
     * 根据ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    UserResponseDTO getUserById(Long userId);

    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    UserResponseDTO getUserByUsername(String username);
}
