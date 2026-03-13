package com.fragmentwords.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fragmentwords.mapper.UserMapper;
import com.fragmentwords.model.dto.UserLoginDTO;
import com.fragmentwords.model.dto.UserLoginResponseDTO;
import com.fragmentwords.model.dto.UserRegisterDTO;
import com.fragmentwords.model.dto.UserResponseDTO;
import com.fragmentwords.model.entity.User;
import com.fragmentwords.service.UserService;
import com.fragmentwords.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 用户Service实现
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserResponseDTO register(UserRegisterDTO registerDTO) {
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, registerDTO.getUsername());
        if (userMapper.selectCount(usernameWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 检查手机号是否已存在
        if (registerDTO.getPhone() != null) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, registerDTO.getPhone());
            if (userMapper.selectCount(phoneWrapper) > 0) {
                throw new RuntimeException("手机号已被注册");
            }
        }

        // 3. 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(encryptPassword(registerDTO.getPassword()));
        user.setPhone(registerDTO.getPhone());
        user.setDeviceId(registerDTO.getDeviceId());
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());

        userMapper.insert(user);

        // 4. 返回用户信息
        return convertToDTO(user);
    }

    @Override
    public UserLoginResponseDTO login(UserLoginDTO loginDTO) {
        // 1. 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 验证密码
        if (!verifyPassword(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 3. 更新设备ID
        if (loginDTO.getDeviceId() != null) {
            user.setDeviceId(loginDTO.getDeviceId());
            user.setUpdateTime(new Date());
            userMapper.updateById(user);
        }

        // 4. 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 5. 构建响应
        UserLoginResponseDTO response = new UserLoginResponseDTO();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setToken(token);
        response.setPhone(user.getPhone());

        return response;
    }

    @Override
    public UserResponseDTO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        return convertToDTO(user);
    }

    @Override
    public UserResponseDTO getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            return null;
        }
        return convertToDTO(user);
    }

    /**
     * 加密密码（简单的Base64编码，生产环境应使用BCrypt等）
     */
    private String encryptPassword(String plainPassword) {
        // TODO: 生产环境应使用BCrypt等安全加密方式
        // 这里使用简单的Base64编码作为演示
        return java.util.Base64.getEncoder().encodeToString(plainPassword.getBytes());
    }

    /**
     * 验证密码
     */
    private boolean verifyPassword(String plainPassword, String encryptedPassword) {
        String encrypted = java.util.Base64.getEncoder().encodeToString(plainPassword.getBytes());
        return encrypted.equals(encryptedPassword);
    }

    /**
     * 转换为DTO
     */
    private UserResponseDTO convertToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPhone(user.getPhone());
        dto.setDeviceId(user.getDeviceId());
        return dto;
    }
}
