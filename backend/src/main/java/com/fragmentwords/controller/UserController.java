package com.fragmentwords.controller;

import com.fragmentwords.annotation.RequireAuth;
import com.fragmentwords.common.ForbiddenException;
import com.fragmentwords.common.Result;
import com.fragmentwords.common.UnauthorizedException;
import com.fragmentwords.config.JwtAuthInterceptor;
import com.fragmentwords.model.dto.UserLoginDTO;
import com.fragmentwords.model.dto.UserLoginResponseDTO;
import com.fragmentwords.model.dto.UserRegisterDTO;
import com.fragmentwords.model.dto.UserResponseDTO;
import com.fragmentwords.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Auth", description = "Authentication and user info endpoints")
@RestController
@RequestMapping(value = "/api/v1/auth", produces = "application/json;charset=UTF-8")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Register user", description = "Create a new user account")
    @PostMapping("/register")
    public Result<UserResponseDTO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        return Result.success(userService.register(registerDTO));
    }

    @Operation(summary = "Login user", description = "Authenticate with username and password")
    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }

    @Operation(summary = "Get user info", description = "Fetch the currently authenticated user")
    @RequireAuth
    @GetMapping("/info/{userId}")
    public Result<UserResponseDTO> getUserInfo(@PathVariable Long userId, HttpServletRequest request) {
        Long authenticatedUserId = resolveAuthenticatedUserId(request);
        if (authenticatedUserId == null) {
            throw new UnauthorizedException("Authentication required");
        }
        if (!authenticatedUserId.equals(userId)) {
            throw new ForbiddenException("You can only access the current authenticated user");
        }
        return Result.success(userService.getUserById(userId));
    }

    private Long resolveAuthenticatedUserId(HttpServletRequest request) {
        Object attribute = request.getAttribute(JwtAuthInterceptor.AUTHENTICATED_USER_ID);
        if (attribute instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
