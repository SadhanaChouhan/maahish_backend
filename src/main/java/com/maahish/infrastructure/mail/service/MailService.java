package com.maahish.infrastructure.mail.service;

import com.maahish.common.constants.AppConstants;
import com.maahish.common.exception.MailDeliveryException;
import com.maahish.order.entity.Order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import org.springframework.scheduling.annotation.Async;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private static final String RESEND_API_URL =
            "https://api.resend.com/emails";

    private static final DateTimeFormatter ORDER_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${resend.from}")
    private String resendFrom;

    private final EmailTemplateService emailTemplateService;

    private final RestTemplate restTemplate = new RestTemplate();

    // ================= OTP EMAIL =================

    public void sendOtpEmail(String to, String otp, String purpose) {

        sendHtmlRequired(
                to,
                AppConstants.BRAND_NAME + " - Your OTP for " + purpose,
                emailTemplateService.otpVerification(otp, purpose)
        );
    }

    // ================= WELCOME EMAIL =================

    @Async
    public void sendWelcomeEmail(String to, String name) {

        sendHtml(
                to,
                "Welcome to " + AppConstants.BRAND_NAME,
                emailTemplateService.welcomeEmail(name)
        );
    }

    // ================= SELLER EMAILS =================

    @Async
    public void sendSellerRegistrationConfirmation(
            String to, String businessName) {

        sendHtml(
                to,
                AppConstants.BRAND_NAME + " Seller Registration Received",
                emailTemplateService.sellerRegistrationReceived(businessName)
        );
    }

    @Async
    public void sendAdminNewSellerEmail(
            String to,
            String ownerName,
            String businessName,
            String registrationDate,
            String sellerUrl) {

        sendHtml(
                to,
                AppConstants.BRAND_NAME + " - New Seller Registration",
                emailTemplateService.adminNewSeller(
                        ownerName,
                        businessName,
                        registrationDate,
                        sellerUrl
                )
        );
    }

    @Async
    public void sendSellerApprovalEmail(String to, String businessName) {

        sendHtml(
                to,
                AppConstants.BRAND_NAME + " Seller Account Approved",
                emailTemplateService.sellerApproved(businessName)
        );
    }

    @Async
    public void sendSellerRejectionEmail(
            String to, String businessName, String reason) {

        sendHtml(
                to,
                AppConstants.BRAND_NAME + " Seller Registration Update",
                emailTemplateService.sellerRejected(businessName, reason)
        );
    }

    @Async
    public void sendSellerDeactivationEmail(
            String to,
            String businessName,
            String statusLabel,
            String reason) {

        sendHtml(
                to,
                AppConstants.BRAND_NAME + " - Seller Account " + statusLabel,
                emailTemplateService.sellerDeactivated(
                        businessName, statusLabel, reason
                )
        );
    }

    // ================= ORDER EMAILS =================

    @Async
    public void sendNewOrderEmail(
            String to,
            String recipientName,
            Order order,
            String sellerNames,
            String actionUrl) {

        sendHtmlCritical(
                to,
                AppConstants.BRAND_NAME + " - New Order " + order.getOrderNumber(),
                emailTemplateService.newOrder(
                        recipientName,
                        order.getOrderNumber(),
                        order.getUser().getName(),
                        sellerNames,
                        order.getTotal().toPlainString(),
                        order.getCreatedAt().format(ORDER_DATE_FORMAT),
                        actionUrl
                )
        );
    }

    @Async
    public void sendOrderConfirmedEmail(
            String to, String customerName, String orderNumber) {

        sendHtmlCritical(
                to,
                AppConstants.BRAND_NAME + " - Order Confirmed (" + orderNumber + ")",
                emailTemplateService.orderConfirmed(customerName, orderNumber)
        );
    }

    @Async
    public void sendOrderDeliveredEmail(
            String to, String customerName, String orderNumber) {

        sendHtmlCritical(
                to,
                AppConstants.BRAND_NAME + " - Order Delivered (" + orderNumber + ")",
                emailTemplateService.orderDelivered(customerName, orderNumber)
        );
    }

    @Async
    public void sendOrderUpdateEmail(
            String to,
            String customerName,
            String orderNumber,
            String subject,
            String body) {

        sendHtmlCritical(
                to,
                AppConstants.BRAND_NAME + " - " + subject + " (" + orderNumber + ")",
                emailTemplateService.genericNotification(
                        subject,
                        "Dear " + customerName + ",\n\n" +
                                body + "\n\nOrder Number: " + orderNumber
                )
        );
    }

    @Async
    public void sendNotificationEmail(
            String to, String subject, String body) {

        sendHtml(
                to,
                AppConstants.BRAND_NAME + " - " + subject,
                emailTemplateService.genericNotification(subject, body)
        );
    }

    // ================= EMAIL SENDING =================

    private void sendHtml(String to, String subject, String htmlBody) {

        sendHtmlInternal(to, subject, htmlBody, false);
    }

    private void sendHtmlCritical(
            String to, String subject, String htmlBody) {

        sendHtmlInternal(to, subject, htmlBody, true);
    }

    private void sendHtmlRequired(
            String to, String subject, String htmlBody) {

        try {

            sendResendEmail(to, subject, htmlBody);

            log.info("OTP email sent successfully to {} - {}", to, subject);

        } catch (RestClientException ex) {

            log.error(
                    "CRITICAL: Failed to send OTP email to {} - {}: {}",
                    to, subject, ex.getMessage(), ex
            );

            throw new MailDeliveryException(
                    "Unable to send OTP email. Please try again later or contact support.",
                    ex
            );
        }
    }

    private void sendHtmlInternal(
            String to,
            String subject,
            String htmlBody,
            boolean critical) {

        try {

            sendResendEmail(to, subject, htmlBody);

            log.info("Email sent successfully to {} - {}", to, subject);

        } catch (RestClientException ex) {

            if (critical) {

                log.error(
                        "CRITICAL: Failed to send transactional email to {} - {}: {}",
                        to, subject, ex.getMessage(), ex
                );

            } else {

                log.error(
                        "Failed to send email to {} - {}: {}",
                        to, subject, ex.getMessage(), ex
                );
            }
        }
    }

    // ================= RESEND API =================

    private void sendResendEmail(
            String to,
            String subject,
            String htmlBody) {

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);
        headers.set("User-Agent", "Maahish-Backend/1.0");

        Map<String, Object> requestBody = Map.of(
                "from", resendFrom,
                "to", List.of(to),
                "subject", subject,
                "html", htmlBody
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(requestBody, headers);

        restTemplate.postForEntity(
                RESEND_API_URL,
                request,
                String.class
        );
    }
}

