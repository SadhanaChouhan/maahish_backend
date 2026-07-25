package com.maahish.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendOtpEmail(String to, String otp, String purpose) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Maahish - Your OTP for " + purpose);
            message.setText("""
                    Dear Customer,

                    Your OTP for %s is: %s

                    This OTP is valid for 10 minutes. Do not share it with anyone.

                    Warm regards,
                    Team Maahish
                    """.formatted(purpose, otp));
            mailSender.send(message);
            log.info("OTP email sent to {}", to);
        } catch (Exception ex) {
            log.error("Failed to send OTP email to {}: {}", to, ex.getMessage());
            log.debug("OTP for {} (dev fallback): {}", to, otp);
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Welcome to Maahish");
            message.setText("""
                    Dear %s,

                    Welcome to Maahish! Your account has been verified successfully.

                    Explore our collection of authentic Maheshwari Cotton Silk Sarees.

                    Warm regards,
                    Team Maahish
                    """.formatted(name));
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send welcome email: {}", ex.getMessage());
        }
    }

    @Async
    public void sendSellerRegistrationConfirmation(String to, String businessName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Maahish Seller Registration Received");
            message.setText("""
                    Dear %s,

                    Thank you for registering as a seller on Maahish.

                    Your application is currently under review. Our admin team will verify your details and notify you once your account is approved.

                    You will not be able to log in until your seller account is approved.

                    Warm regards,
                    Team Maahish
                    """.formatted(businessName));
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send seller registration email: {}", ex.getMessage());
        }
    }

    @Async
    public void sendSellerApprovalEmail(String to, String businessName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Maahish Seller Account Approved");
            message.setText("""
                    Dear %s,

                    Congratulations! Your Maahish seller account has been approved.

                    You can now log in and access your seller dashboard to add products and manage orders.

                    Warm regards,
                    Team Maahish
                    """.formatted(businessName));
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send seller approval email: {}", ex.getMessage());
        }
    }

    @Async
    public void sendSellerRejectionEmail(String to, String businessName, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Maahish Seller Registration Update");
            message.setText("""
                    Dear %s,

                    We regret to inform you that your seller registration on Maahish could not be approved at this time.

                    Reason: %s

                    If you believe this is an error, please contact our support team.

                    Warm regards,
                    Team Maahish
                    """.formatted(businessName, reason != null ? reason : "Not specified"));
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send seller rejection email: {}", ex.getMessage());
        }
    }

    public void sendNotificationEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Maahish - " + subject);
            message.setText(body + "\n\nWarm regards,\nTeam Maahish");
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send notification email to {}: {}", to, ex.getMessage());
        }
    }
}
