package com.fragmentwords.controller;

import com.fragmentwords.common.ConflictException;
import com.fragmentwords.common.ForbiddenException;
import com.fragmentwords.common.ResourceNotFoundException;
import com.fragmentwords.common.UnauthorizedException;
import com.fragmentwords.config.GlobalExceptionHandler;
import com.fragmentwords.model.dto.UserLoginResponseDTO;
import com.fragmentwords.model.dto.UserResponseDTO;
import com.fragmentwords.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.fragmentwords.config.JwtAuthInterceptor.AUTHENTICATED_USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private MockMvc mockMvc;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        UserController controller = new UserController();
        ReflectionTestUtils.setField(controller, "userService", userService);

        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .build();
    }

    @Test
    void registerValidationReturnsHttp400() throws Exception {
        mockMvc.perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"password\":\"secret123\"}")
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void registerConflictReturnsHttp409() throws Exception {
        when(userService.register(any())).thenThrow(new ConflictException("username exists"));

        mockMvc.perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"alice\",\"password\":\"secret123\"}")
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(409))
            .andExpect(jsonPath("$.message").value("username exists"));
    }

    @Test
    void loginUnauthorizedReturnsHttp401() throws Exception {
        when(userService.login(any())).thenThrow(new UnauthorizedException("invalid credentials"));

        mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"alice\",\"password\":\"wrong-password\"}")
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value("invalid credentials"));
    }

    @Test
    void getUserInfoRequiresAuthenticatedUserId() throws Exception {
        mockMvc.perform(get("/api/v1/auth/info/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void getUserInfoRejectsOtherUsers() throws Exception {
        mockMvc.perform(
                get("/api/v1/auth/info/2")
                    .with(authenticatedUser(1L))
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(403))
            .andExpect(jsonPath("$.message").value("You can only access the current authenticated user"));
    }

    @Test
    void getUserInfoReturnsHttp404WhenUserIsMissing() throws Exception {
        when(userService.getUserById(1L)).thenThrow(new ResourceNotFoundException("missing"));

        mockMvc.perform(
                get("/api/v1/auth/info/1")
                    .with(authenticatedUser(1L))
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("missing"));
    }

    @Test
    void getUserInfoReturnsCurrentUser() throws Exception {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(1L);
        response.setUsername("alice");

        when(userService.getUserById(eq(1L))).thenReturn(response);

        mockMvc.perform(
                get("/api/v1/auth/info/1")
                    .with(authenticatedUser(1L))
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.username").value("alice"));
    }

    @Test
    void loginReturnsTokenPayloadOnSuccess() throws Exception {
        UserLoginResponseDTO response = new UserLoginResponseDTO();
        response.setUserId(1L);
        response.setUsername("alice");
        response.setToken("jwt-token");

        when(userService.login(any())).thenReturn(response);

        mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"alice\",\"password\":\"secret123\"}")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.userId").value(1))
            .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    private RequestPostProcessor authenticatedUser(Long userId) {
        return request -> {
            request.setAttribute(AUTHENTICATED_USER_ID, userId);
            return request;
        };
    }
}
