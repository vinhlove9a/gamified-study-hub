package com.gamifiedstudyhub.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * The {@link PasswordEncoder} lives in its own config (not on {@code SecurityConfig})
 * so beans wired into the security filter chain — e.g. the OAuth success handler and,
 * through it, MfaService — can depend on it without forming a bean-creation cycle.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
