package com.gamifiedstudyhub.backend.identity;

import com.gamifiedstudyhub.backend.authz.service.AuthorityService;
import com.gamifiedstudyhub.backend.common.util.DateTimeUtils;
import com.gamifiedstudyhub.backend.identity.entity.AuthIdentity;
import com.gamifiedstudyhub.backend.identity.repository.AuthIdentityRepository;
import com.gamifiedstudyhub.backend.user.entity.User;
import com.gamifiedstudyhub.backend.user.entity.UserStatus;
import com.gamifiedstudyhub.backend.user.repository.UserRepository;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Find-or-create a local user for a federated (social) login, linking by verified email. */
@Service
public class FederatedIdentityService {

    private final AuthIdentityRepository identityRepository;
    private final UserRepository userRepository;
    private final AuthorityService authorityService;

    public FederatedIdentityService(
            AuthIdentityRepository identityRepository,
            UserRepository userRepository,
            AuthorityService authorityService
    ) {
        this.identityRepository = identityRepository;
        this.userRepository = userRepository;
        this.authorityService = authorityService;
    }

    @Transactional
    public User resolve(String provider, String providerUserId, String email, boolean emailVerified, String fullName) {
        // 1) Already linked → return that user.
        AuthIdentity existing = identityRepository.findByProviderAndProviderUserId(provider, providerUserId).orElse(null);
        if (existing != null) {
            return userRepository.findByIdAndDeletedAtIsNull(existing.getUserId())
                    .orElseThrow(() -> new IllegalStateException("Linked user not found"));
        }

        String normalizedEmail = email == null ? null : email.trim().toLowerCase(Locale.ROOT);

        // 2) Existing local account with the same (provider-verified) email → link it.
        if (normalizedEmail != null && emailVerified) {
            User byEmail = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(normalizedEmail).orElse(null);
            if (byEmail != null) {
                link(provider, providerUserId, normalizedEmail, byEmail);
                if (!byEmail.isEmailVerified()) {
                    byEmail.setEmailVerified(true);
                    byEmail.setEmailVerifiedAt(DateTimeUtils.nowUtc());
                    userRepository.save(byEmail);
                }
                return byEmail;
            }
        }

        // 3) Create a fresh passwordless account.
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(null);
        user.setFullName(fullName == null || fullName.isBlank() ? "User" : fullName.trim());
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(emailVerified);
        user.setEmailVerifiedAt(emailVerified ? DateTimeUtils.nowUtc() : null);
        User saved = userRepository.save(user);
        authorityService.assignDefaultRole(saved.getId());
        link(provider, providerUserId, normalizedEmail, saved);
        return saved;
    }

    private void link(String provider, String providerUserId, String email, User user) {
        AuthIdentity identity = new AuthIdentity();
        identity.setUserId(user.getId());
        identity.setProvider(provider);
        identity.setProviderUserId(providerUserId);
        identity.setEmail(email);
        identity.setCreatedAt(DateTimeUtils.nowUtc());
        identityRepository.save(identity);
    }
}
