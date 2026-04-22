package com.fragmentwords.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fragmentwords.common.ConflictException;
import com.fragmentwords.common.ResourceNotFoundException;
import com.fragmentwords.common.UnauthorizedException;
import com.fragmentwords.mapper.UserMapper;
import com.fragmentwords.model.dto.UserLoginDTO;
import com.fragmentwords.model.dto.UserLoginResponseDTO;
import com.fragmentwords.model.dto.UserRegisterDTO;
import com.fragmentwords.model.dto.UserResponseDTO;
import com.fragmentwords.model.entity.User;
import com.fragmentwords.service.UserService;
import com.fragmentwords.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserResponseDTO register(UserRegisterDTO registerDTO) {
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, registerDTO.getUsername());
        if (userMapper.selectCount(usernameWrapper) > 0) {
            throw new ConflictException("Username already exists");
        }

        if (registerDTO.getPhone() != null) {
            LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(User::getPhone, registerDTO.getPhone());
            if (userMapper.selectCount(phoneWrapper) > 0) {
                throw new ConflictException("Phone number already registered");
            }
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(encryptPassword(registerDTO.getPassword()));
        user.setPhone(registerDTO.getPhone());
        user.setDeviceId(registerDTO.getDeviceId());
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());

        userMapper.insert(user);
        return convertToDTO(user);
    }

    @Override
    public UserLoginResponseDTO login(UserLoginDTO loginDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null || !verifyPassword(loginDTO.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        if (loginDTO.getDeviceId() != null) {
            user.setDeviceId(loginDTO.getDeviceId());
            user.setUpdateTime(new Date());
            userMapper.updateById(user);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

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
            throw new ResourceNotFoundException("User not found: " + userId);
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

    // Temporary password encoding for local development only.
    private String encryptPassword(String plainPassword) {
        return Base64.getEncoder().encodeToString(plainPassword.getBytes());
    }

    private boolean verifyPassword(String plainPassword, String encryptedPassword) {
        String encrypted = Base64.getEncoder().encodeToString(plainPassword.getBytes());
        return encrypted.equals(encryptedPassword);
    }

    private UserResponseDTO convertToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPhone(user.getPhone());
        dto.setDeviceId(user.getDeviceId());
        return dto;
    }
}
