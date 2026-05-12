package com.gamifiedstudyhub.backend.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import com.gamifiedstudyhub.backend.common.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public static final String AUTH_ERROR_CODE_ATTRIBUTE = "auth.error.code";
    public static final String AUTH_ERROR_MESSAGE_ATTRIBUTE = "auth.error.message";

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        String code = (String) request.getAttribute(AUTH_ERROR_CODE_ATTRIBUTE);
        String message = (String) request.getAttribute(AUTH_ERROR_MESSAGE_ATTRIBUTE);

        if (code == null || code.isBlank()) {
            code = ErrorCodes.UNAUTHORIZED;
        }
        if (message == null || message.isBlank()) {
            message = "Unauthorized";
        }

        ErrorResponse errorResponse = new ErrorResponse(
                code,
                message,
                request.getRequestURI(),
                List.of()
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
