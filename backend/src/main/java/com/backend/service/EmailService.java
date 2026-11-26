package com.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvitationEmail(
            String recipientEmail,
            String role,
            String invitationLink) {

        if (!mailEnabled) {
            throw new IllegalStateException(
                    "Email service is disabled. Configure Gmail SMTP first."
            );
        }

        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException(
                    "Sender email is not configured."
            );
        }

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(recipientEmail);
        message.setSubject("You're invited to join FarmVerse Nexus");

        message.setText(
                "Hello,\n\n" +
                "You have been invited to join FarmVerse Nexus as a " +
                role + ".\n\n" +
                "Click the link below to accept your invitation and create your account:\n\n" +
                invitationLink + "\n\n" +
                "This invitation link is valid for 7 days.\n\n" +
                "If you were not expecting this invitation, you can ignore this email.\n\n" +
                "Regards,\nFarmVerse Nexus Team"
        );

        try {
            mailSender.send(message);
        } catch (MailAuthenticationException ex) {
            // Never log credentials or the invitation link/token.
            log.error("SMTP authentication was rejected by the mail server: {}", ex.getMessage());
            throw new IllegalStateException(
                    "The mail server rejected the sender login. Check MAIL_USERNAME and the "
                    + "Gmail App Password (16 lowercase letters, generated for that same account).",
                    ex);
        } catch (MailException ex) {
            log.error("Invitation email could not be sent ({}): {}",
                    ex.getClass().getSimpleName(), ex.getMessage());
            throw new IllegalStateException(
                    "The invitation email could not be sent (" + ex.getClass().getSimpleName()
                    + "). Check the SMTP host, port and network access.",
                    ex);
        }
    }
}