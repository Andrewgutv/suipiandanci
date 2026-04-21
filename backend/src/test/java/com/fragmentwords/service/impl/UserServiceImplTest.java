package com.fragmentwords.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fragmentwords.common.ResourceNotFoundException;
import com.fragmentwords.mapper.UserMapper;
import com.fragmentwords.model.dto.UserLoginDTO;
import com.fragmentwords.model.dto.UserRegisterDTO;
import com.fragmentwords.model.entity.User;
import com.fragmentwords.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerThrowsConflictWhenUsernameAlreadyExists() {
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("alice");
        registerDTO.setPassword("secret123");

        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> userService.register(registerDTO));
        assertEquals("Username already exists", exception.getMessage());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void registerThrowsConflictWhenPhoneAlreadyExists() {
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("alice");
        registerDTO.setPassword("secret123");
        registerDTO.setPhone("13800138000");

        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L, 1L);

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> userService.register(registerDTO));
        assertEquals("Phone number already registered", exception.getMessage());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void loginThrowsUnauthorizedWhenUserDoesNotExist() {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("alice");
        loginDTO.setPassword("secret123");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        SecurityException exception = assertThrows(SecurityException.class, () -> userService.login(loginDTO));
        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void loginThrowsUnauthorizedWhenPasswordIsWrong() {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("alice");
        loginDTO.setPassword("wrong-password");

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword(java.util.Base64.getEncoder().encodeToString("secret123".getBytes()));

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        SecurityException exception = assertThrows(SecurityException.class, () -> userService.login(loginDTO));
        assertEquals("Invalid username or password", exception.getMessage());
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    void getUserByIdThrowsNotFoundWhenMissing() {
        when(userMapper.selectById(42L)).thenReturn(null);

        ResourceNotFoundException exception =
            assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(42L));
        assertEquals("User not found: 42", exception.getMessage());
    }
}
