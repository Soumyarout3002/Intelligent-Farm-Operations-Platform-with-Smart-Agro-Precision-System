package com.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

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

        mailSender.send(message);
    }
}