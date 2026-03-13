package com.fragmentwords.controller;

import com.fragmentwords.common.Result;
import com.fragmentwords.model.dto.UserLoginDTO;
import com.fragmentwords.model.dto.UserLoginResponseDTO;
import com.fragmentwords.model.dto.UserRegisterDTO;
import com.fragmentwords.model.dto.UserResponseDTO;
import com.fragmentwords.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户Controller
 */
@Tag(name = "用户管理", description = "用户注册、登录、信息查询")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户注册", description = "创建新用户")
    @PostMapping("/register")
    public Result<UserResponseDTO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        try {
            UserResponseDTO user = userService.register(registerDTO);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT Token")
    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        try {
            UserLoginResponseDTO response = userService.login(loginDTO);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户信息")
    @GetMapping("/info/{userId}")
    public Result<UserResponseDTO> getUserInfo(@PathVariable Long userId) {
        UserResponseDTO user = userService.getUserById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }
}
