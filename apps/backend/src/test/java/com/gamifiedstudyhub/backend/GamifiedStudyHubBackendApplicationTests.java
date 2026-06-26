package com.gamifiedstudyhub.backend;

import com.gamifiedstudyhub.backend.auth.repository.EmailVerificationTokenRepository;
import com.gamifiedstudyhub.backend.auth.repository.PasswordResetTokenRepository;
import com.gamifiedstudyhub.backend.authz.repository.RoleRepository;
import com.gamifiedstudyhub.backend.authz.repository.UserRoleRepository;
import com.gamifiedstudyhub.backend.document.repository.DocumentCategoryRepository;
import com.gamifiedstudyhub.backend.document.repository.DocumentRepository;
import com.gamifiedstudyhub.backend.document.repository.DocumentTagRelationRepository;
import com.gamifiedstudyhub.backend.document.repository.TagRepository;
import com.gamifiedstudyhub.backend.user.repository.UserRepository;
import com.gamifiedstudyhub.backend.workspace.repository.WorkspaceMemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Boots the full application context without a database (DataSource/JPA/Flyway
 * auto-config excluded) by mocking every Spring Data repository the bean graph
 * needs. Add a {@code @MockBean} here whenever a new repository is introduced.
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
        "app.jpa.auditing.enabled=false"
})
class GamifiedStudyHubBackendApplicationTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @MockBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private UserRoleRepository userRoleRepository;

    @MockBean
    private WorkspaceMemberRepository workspaceMemberRepository;

    @MockBean
    private DocumentRepository documentRepository;

    @MockBean
    private DocumentCategoryRepository documentCategoryRepository;

    @MockBean
    private TagRepository tagRepository;

    @MockBean
    private DocumentTagRelationRepository documentTagRelationRepository;

    @Test
    void contextLoads() {
    }
}
