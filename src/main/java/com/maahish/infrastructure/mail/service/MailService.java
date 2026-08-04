package com.maahish.infrastructure.mail.service;

import com.maahish.common.constants.AppConstants;
import com.maahish.common.exception.MailDeliveryException;
import com.maahish.order.entity.Order;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private static final DateTimeFormatter ORDER_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final JavaMailSender mailSender;
    private final EmailTemplateService emailTemplateService;

    public void sendOtpEmail(String to, String otp, String purpose) {
        sendHtmlRequired(to, AppConstants.BRAND_NAME + " - Your OTP for " + purpose,
                emailTemplateService.otpVerification(otp, purpose));
    }

    @Async
    public void sendWelcomeEmail(String to, String name) {
        sendHtml(to, "Welcome to " + AppConstants.BRAND_NAME,
                emailTemplateService.welcomeEmail(name));
    }

    @Async
    public void sendSellerRegistrationConfirmation(String to, String businessName) {
        sendHtml(to, AppConstants.BRAND_NAME + " Seller Registration Received",
                emailTemplateService.sellerRegistrationReceived(businessName));
    }

    @Async
    public void sendAdminNewSellerEmail(String to, String ownerName, String businessName,
                                        String registrationDate, String sellerUrl) {
        sendHtml(to, AppConstants.BRAND_NAME + " - New Seller Registration",
                emailTemplateService.adminNewSeller(ownerName, businessName, registrationDate, sellerUrl));
    }

    @Async
    public void sendSellerApprovalEmail(String to, String businessName) {
        sendHtml(to, AppConstants.BRAND_NAME + " Seller Account Approved",
                emailTemplateService.sellerApproved(businessName));
    }

    @Async
    public void sendSellerRejectionEmail(String to, String businessName, String reason) {
        sendHtml(to, AppConstants.BRAND_NAME + " Seller Registration Update",
                emailTemplateService.sellerRejected(businessName, reason));
    }

    @Async
    public void sendSellerDeactivationEmail(String to, String businessName, String statusLabel, String reason) {
        sendHtml(to, AppConstants.BRAND_NAME + " - Seller Account " + statusLabel,
                emailTemplateService.sellerDeactivated(businessName, statusLabel, reason));
    }

    @Async
    public void sendNewOrderEmail(String to, String recipientName, Order order, String sellerNames, String actionUrl) {
        sendHtmlCritical(to, AppConstants.BRAND_NAME + " - New Order " + order.getOrderNumber(),
                emailTemplateService.newOrder(
                        recipientName,
                        order.getOrderNumber(),
                        order.getUser().getName(),
                        sellerNames,
                        order.getTotal().toPlainString(),
                        order.getCreatedAt().format(ORDER_DATE_FORMAT),
                        actionUrl
                ));
    }

    @Async
    public void sendOrderConfirmedEmail(String to, String customerName, String orderNumber) {
        sendHtmlCritical(to, AppConstants.BRAND_NAME + " - Order Confirmed (" + orderNumber + ")",
                emailTemplateService.orderConfirmed(customerName, orderNumber));
    }

    @Async
    public void sendOrderDeliveredEmail(String to, String customerName, String orderNumber) {
        sendHtmlCritical(to, AppConstants.BRAND_NAME + " - Order Delivered (" + orderNumber + ")",
                emailTemplateService.orderDelivered(customerName, orderNumber));
    }

    @Async
    public void sendOrderUpdateEmail(String to, String customerName, String orderNumber, String subject, String body) {
        sendHtmlCritical(to, AppConstants.BRAND_NAME + " - " + subject + " (" + orderNumber + ")",
                emailTemplateService.genericNotification(subject,
                        "Dear " + customerName + ",\n\n" + body + "\n\nOrder Number: " + orderNumber));
    }

    @Async
    public void sendNotificationEmail(String to, String subject, String body) {
        sendHtml(to, AppConstants.BRAND_NAME + " - " + subject,
                emailTemplateService.genericNotification(subject, body));
    }

    private void sendHtml(String to, String subject, String htmlBody) {
        sendHtmlInternal(to, subject, htmlBody, false);
    }

    private void sendHtmlCritical(String to, String subject, String htmlBody) {
        sendHtmlInternal(to, subject, htmlBody, true);
    }

    private void sendHtmlRequired(String to, String subject, String htmlBody) {
        try {
            sendMimeMessage(to, subject, htmlBody);
            log.info("OTP email sent to {} — {}", to, subject);
        } catch (MessagingException ex) {
            log.error("CRITICAL: Failed to send OTP email to {} — {}: {}", to, subject, ex.getMessage(), ex);
            throw new MailDeliveryException("Unable to send OTP email. Please try again later or contact support.", ex);
        }
    }

    private void sendHtmlInternal(String to, String subject, String htmlBody, boolean critical) {
        try {
            sendMimeMessage(to, subject, htmlBody);
            log.info("Email sent to {} — {}", to, subject);
        } catch (MessagingException ex) {
            if (critical) {
                log.error("CRITICAL: Failed to send transactional email to {} — {}: {}",
                        to, subject, ex.getMessage(), ex);
            } else {
                log.error("Failed to send email to {} — {}: {}", to, subject, ex.getMessage(), ex);
            }
        }
    }

    private void sendMimeMessage(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }
}
