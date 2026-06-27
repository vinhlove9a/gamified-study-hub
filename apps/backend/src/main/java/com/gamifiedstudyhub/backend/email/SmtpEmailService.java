package com.gamifiedstudyhub.backend.email;

import com.gamifiedstudyhub.backend.user.entity.User;
import jakarta.mail.internet.MimeMessage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * SMTP email via JavaMailSender + Thymeleaf. Best-effort and asynchronous:
 * sending never blocks or fails the request. If no mail server is configured
 * (MAIL_HOST empty → no JavaMailSender bean), it logs the link instead of
 * sending — so the app runs fine in dev / before SMTP creds are provided.
 */
@Service
public class SmtpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final TemplateEngine templateEngine;
    private final EmailProperties properties;

    public SmtpEmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            TemplateEngine templateEngine,
            EmailProperties properties
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.templateEngine = templateEngine;
        this.properties = properties;
    }

    @Override
    @Async("mailExecutor")
    public void sendVerificationEmail(User user, String rawToken) {
        String link = properties.getBaseUrl() + "/auth/verify-email?token=" + encode(rawToken);
        Context ctx = new Context();
        ctx.setVariable("fullName", user.getFullName());
        ctx.setVariable("link", link);
        send(user.getEmail(), "Xác thực email của bạn", "email/verify-email", ctx, link);
    }

    @Override
    @Async("mailExecutor")
    public void sendPasswordResetEmail(User user, String rawToken) {
        String link = properties.getBaseUrl() + "/auth/reset-password?token=" + encode(rawToken);
        Context ctx = new Context();
        ctx.setVariable("fullName", user.getFullName());
        ctx.setVariable("link", link);
        send(user.getEmail(), "Đặt lại mật khẩu", "email/reset-password", ctx, link);
    }

    private void send(String to, String subject, String template, Context ctx, String link) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.info("[email disabled] would send '{}' to {} — link: {}", subject, to, link);
            return;
        }
        try {
            String html = templateEngine.process(template, ctx);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Sent '{}' email to {}", subject, to);
        } catch (Exception e) {
            log.warn("Failed to send '{}' email to {} (ignored): {}", subject, to, e.getMessage());
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
