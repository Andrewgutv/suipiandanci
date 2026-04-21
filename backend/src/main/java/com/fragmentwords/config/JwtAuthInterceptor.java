package com.fragmentwords.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fragmentwords.annotation.RequireAuth;
import com.fragmentwords.common.Result;
import com.fragmentwords.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {
    public static final String AUTHENTICATED_USER_ID = "authenticatedUserId";
    public static final String AUTHENTICATED_USERNAME = "authenticatedUsername";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && !authorization.isBlank()) {
            if (!authorization.startsWith("Bearer ")) {
                return writeUnauthorized(response, "Authorization header must use Bearer token");
            }

            String token = authorization.substring(7).trim();
            if (token.isEmpty() || !jwtUtil.validateToken(token)) {
                return writeUnauthorized(response, "Invalid or expired token");
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return writeUnauthorized(response, "Invalid token subject");
            }

            request.setAttribute(AUTHENTICATED_USER_ID, userId);
            request.setAttribute(AUTHENTICATED_USERNAME, jwtUtil.getUsernameFromToken(token));
        }

        boolean authRequired = handlerMethod.hasMethodAnnotation(RequireAuth.class)
            || handlerMethod.getBeanType().isAnnotationPresent(RequireAuth.class);
        if (authRequired && request.getAttribute(AUTHENTICATED_USER_ID) == null) {
            return writeUnauthorized(response, "Authentication required");
        }

        return true;
    }

    private boolean writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Result.unauthorized(message));
        return false;
    }

}
