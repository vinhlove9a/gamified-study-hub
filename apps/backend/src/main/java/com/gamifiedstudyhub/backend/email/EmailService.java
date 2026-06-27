package com.gamifiedstudyhub.backend.email;

import com.gamifiedstudyhub.backend.user.entity.User;

public interface EmailService {

    void sendVerificationEmail(User user, String rawToken);

    void sendPasswordResetEmail(User user, String rawToken);
}
