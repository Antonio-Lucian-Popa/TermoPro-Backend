package com.asusoftware.TermoPro.mail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendInvitationEmail(String to, String inviteLink, String companyName, String role) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("You're invited to join " + companyName + " on Termo Pro!");

            String html = buildInvitationHtml(inviteLink, companyName, role);
            helper.setText(html, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendEmail(String to, String subject, String body) {
        var message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private String buildInvitationHtml(String link, String companyName, String role) {
        return """
        <html>
        <body style="font-family: Arial, sans-serif; background-color: #f9fafb; padding: 20px; color: #333;">
          <div style="max-width: 600px; margin: auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.05);">
            <h2 style="color: #2e7d32;">You're Invited to Join %s!</h2>
            <p>Hello,</p>
            <p><strong>%s</strong> has invited you to join to the company <strong>%s</strong> as a <strong>%s</strong> on TermoPro.</p>
            <p>To accept the invitation and create your account, click the button below:</p>
            <div style="text-align: center; margin: 20px 0;">
              <a href="%s" style="background-color: #2e7d32; color: white; padding: 12px 24px; border-radius: 6px; text-decoration: none; font-weight: bold;">
                Accept Invitation
              </a>
            </div>
            <p>If you did not expect this invitation, you can safely ignore this email.</p>
            <hr style="margin-top: 30px;" />
            <p style="font-size: 12px; color: #888;">Termo Pro · Smart tools for modern clinics</p>
          </div>
        </body>
        </html>
        """.formatted(companyName, companyName, companyName, role, link);
    }
}