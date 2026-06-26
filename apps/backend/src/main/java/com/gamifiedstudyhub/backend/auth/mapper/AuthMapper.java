package com.gamifiedstudyhub.backend.auth.mapper;

import com.gamifiedstudyhub.backend.auth.dto.AuthResponse;
import com.gamifiedstudyhub.backend.auth.dto.UserSummaryResponse;
import com.gamifiedstudyhub.backend.user.entity.User;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    /** Maps a user to its public summary, deriving role/permissions from authority codes. */
    public UserSummaryResponse toUserSummary(User user, Collection<String> authorityCodes) {
        List<String> codes = authorityCodes == null ? List.of() : List.copyOf(authorityCodes);
        List<String> roles = codes.stream().filter(c -> c.startsWith(ROLE_PREFIX)).sorted().toList();
        List<String> permissions = codes.stream().filter(c -> !c.startsWith(ROLE_PREFIX)).sorted().toList();
        String primaryRole = roles.contains(ROLE_ADMIN) ? "admin" : "user";

        return new UserSummaryResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getStatus().name(),
                user.isEmailVerified(),
                primaryRole,
                roles,
                permissions,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public AuthResponse toAuthResponse(
            String accessToken,
            long expiresInSeconds,
            User user,
            Collection<String> authorityCodes
    ) {
        return new AuthResponse(accessToken, "Bearer", expiresInSeconds, toUserSummary(user, authorityCodes));
    }
}
