package com.matrimonial.service;

import com.matrimonial.util.EmailTemplateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * SERVICE: EmailService
 *
 * Handles sending all emails via JavaMailSender (SMTP).
 * Uses HTML templates from EmailTemplateUtil.
 *
 * All email methods are @Async — they run in a background thread
 * so the user's API response is NOT delayed by email sending.
 *
 * Layer: Service (email infrastructure, no business logic)
 *
 * NOTE: Add @EnableAsync to MatrimonialApplication.java to enable @Async.
 */
@Service
@RequiredArgsConstructor
@Slf4j // Lombok: gives us log.info(), log.error() etc.
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateUtil emailTemplateUtil;

    // Sender email address from application.properties
    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send OTP email for Forgot Password flow.
     *
     * @param toEmail recipient's email
     * @param otp     the 6-digit OTP code
     */
    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        String subject = "Your OTP for Password Reset - Prajapati Samaj Matrimonial";
        String body = emailTemplateUtil.buildForgotPasswordEmail(otp);
        sendHtmlEmail(toEmail, subject, body);
    }

    /**
     * Notify a user that someone liked their profile.
     *
     * @param toEmail    receiver's email
     * @param likerName  name of the person who liked
     */
    @Async
    public void sendLikeNotification(String toEmail, String likerName) {
        String subject = likerName + " liked your profile! - Prajapati Samaj";
        String body = emailTemplateUtil.buildLikeNotificationEmail(likerName);
        sendHtmlEmail(toEmail, subject, body);
    }

    /**
     * Notify a user that someone sent them an interest request.
     *
     * @param toEmail     receiver's email
     * @param senderName  name of the person who sent interest
     */
    @Async
    public void sendInterestNotification(String toEmail, String senderName) {
        String subject = senderName + " is interested in you! - Prajapati Samaj";
        String body = emailTemplateUtil.buildInterestNotificationEmail(senderName);
        sendHtmlEmail(toEmail, subject, body);
    }

    /**
     * Notify a user that their interest was accepted (match!).
     *
     * @param toEmail       sender's email (who sent the interest)
     * @param acceptorName  name of the person who accepted
     */
    @Async
    public void sendInterestAcceptedNotification(String toEmail, String acceptorName) {
        String subject = "Your interest was accepted! - Prajapati Samaj";
        String body = emailTemplateUtil.buildInterestAcceptedEmail(acceptorName);
        sendHtmlEmail(toEmail, subject, body);
    }

    // ===== Private helper: Build and send an HTML email =====
    private void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML email

            mailSender.send(message);
            log.info("Email sent to: {}", toEmail);

        } catch (Exception e) {
            // Log error but don't crash the application — email is non-critical
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }
}
