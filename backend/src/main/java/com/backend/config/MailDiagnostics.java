package com.backend.config;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

/**
 * Optional start-up check of the SMTP settings. Runs only when MAIL_DIAGNOSTICS=true.
 * It logs WHERE the mail settings came from and whether the SMTP server accepts the login
 * (connect + authenticate only; no email is sent). It never logs the password, the App Password,
 * or invitation tokens - only the password's length and character-type flags.
 */
@Component
public class MailDiagnostics implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MailDiagnostics.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.diagnostics:false}")
    private boolean enabled;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:}")
    private String from;

    public MailDiagnostics(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        log.info("[MAIL-DIAG] app.mail.enabled={}", mailEnabled);

        if (!(mailSender instanceof JavaMailSenderImpl)) {
            log.warn("[MAIL-DIAG] JavaMailSender is not JavaMailSenderImpl; cannot inspect.");
            return;
        }
        JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;

        String user = impl.getUsername();
        String pass = impl.getPassword();

        log.info("[MAIL-DIAG] host={} port={}", impl.getHost(), impl.getPort());
        log.info("[MAIL-DIAG] smtp.auth={} starttls.enable={} starttls.required={}",
                impl.getJavaMailProperties().getProperty("mail.smtp.auth"),
                impl.getJavaMailProperties().getProperty("mail.smtp.starttls.enable"),
                impl.getJavaMailProperties().getProperty("mail.smtp.starttls.required"));
        log.info("[MAIL-DIAG] MAIL_USERNAME source={} ; MAIL_PASSWORD source={}",
                source("MAIL_USERNAME"), source("MAIL_PASSWORD"));
        log.info("[MAIL-DIAG] username(masked)={} ; from==username: {}", mask(user),
                user != null && user.equalsIgnoreCase(from));

        if (user == null || user.isBlank() || !user.contains("@")) {
            log.warn("[MAIL-DIAG] Username is empty or is not a full e-mail address.");
        } else if (!user.toLowerCase().endsWith("@gmail.com") && !user.toLowerCase().endsWith("@googlemail.com")) {
            log.warn("[MAIL-DIAG] Username is not a @gmail.com address. For Google Workspace accounts "
                    + "the administrator must allow App Passwords / SMTP.");
        }

        if (pass == null || pass.isEmpty()) {
            log.warn("[MAIL-DIAG] Password is EMPTY - MAIL_PASSWORD is not reaching Spring.");
        } else {
            boolean ws = pass.chars().anyMatch(Character::isWhitespace);
            boolean nonAscii = pass.chars().anyMatch(c -> c > 126 || c < 33);
            boolean quote = pass.indexOf('"') >= 0 || pass.indexOf('\'') >= 0;
            boolean appFormat = pass.length() == 16 && pass.chars().allMatch(c -> c >= 'a' && c <= 'z');
            log.info("[MAIL-DIAG] password length={} whitespace={} nonAscii={} quotes={} looksLikeGoogleAppPassword={}",
                    pass.length(), ws, nonAscii, quote, appFormat);
            if (!appFormat) {
                log.warn("[MAIL-DIAG] A Google App Password is exactly 16 lowercase letters (a-z). "
                        + "This value is not in that format - it is probably NOT an App Password "
                        + "(e.g. the normal account password, or text copied with extra characters).");
            }
        }

        try {
            impl.testConnection();
            log.info("[MAIL-DIAG] RESULT: SMTP connection and login ACCEPTED by {}.", impl.getHost());
        } catch (AuthenticationFailedException ex) {
            log.error("[MAIL-DIAG] RESULT: SMTP server REJECTED the username/password (authentication failed). "
                    + "Server said: {}", ex.getMessage());
        } catch (MessagingException ex) {
            log.error("[MAIL-DIAG] RESULT: could not complete SMTP connection ({}): {}",
                    ex.getClass().getSimpleName(), ex.getMessage());
        }
    }

    private static String source(String key) {
        if (System.getenv(key) != null) return "OS environment variable";
        if (System.getProperty(key) != null) return ".env file in the backend folder (loaded at start-up)";
        return "NOT SET";
    }

    private static String mask(String user) {
        if (user == null || user.isBlank()) return "(empty)";
        int at = user.indexOf('@');
        if (at < 1) return "(not an e-mail address, length " + user.length() + ")";
        return user.substring(0, Math.min(2, at)) + "***" + user.substring(at);
    }
}
