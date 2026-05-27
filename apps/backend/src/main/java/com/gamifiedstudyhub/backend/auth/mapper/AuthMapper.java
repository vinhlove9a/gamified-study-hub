package com.gamifiedstudyhub.backend.auth.mapper;

import com.gamifiedstudyhub.backend.auth.dto.AuthResponse;
import com.gamifiedstudyhub.backend.auth.dto.UserSummaryResponse;
import com.gamifiedstudyhub.backend.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public UserSummaryResponse toUserSummary(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getStatus().name(),
                user.isEmailVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public AuthResponse toAuthResponse(String accessToken, long expiresInSeconds, User user) {
        return new AuthResponse(accessToken, "Bearer", expiresInSeconds, toUserSummary(user));
    }
}
