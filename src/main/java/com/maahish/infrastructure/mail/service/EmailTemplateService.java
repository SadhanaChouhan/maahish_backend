package com.maahish.infrastructure.mail.service;

import com.maahish.common.constants.AppConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class EmailTemplateService {

    private static final String MAROON = "#5B1F3A";
    private static final String GOLD = "#C9A66B";
    private static final String IVORY = "#FFF9F4";
    private static final String TEXT = "#3D2B2B";

    public String otpVerification(String otp, String purpose) {
        return layout(
                "Verify Your Email",
                """
                <p style="margin:0 0 16px;color:%s;">Dear Customer,</p>
                <p style="margin:0 0 16px;color:%s;">Use the OTP below to complete <strong>%s</strong> on %s.</p>
                <div style="background:%s;border:2px dashed %s;border-radius:12px;padding:20px;text-align:center;margin:24px 0;">
                  <span style="font-size:32px;font-weight:700;letter-spacing:8px;color:%s;">%s</span>
                </div>
                <p style="margin:0;color:%s;font-size:14px;">This OTP is valid for <strong>10 minutes</strong>. Do not share it with anyone.</p>
                """.formatted(TEXT, TEXT, escape(purpose), AppConstants.BRAND_NAME, IVORY, GOLD, MAROON, escape(otp), TEXT),
                null,
                null
        );
    }

    public String welcomeEmail(String name) {
        return layout(
                "Welcome to " + AppConstants.BRAND_NAME,
                """
                <p style="margin:0 0 16px;color:%s;">Dear %s,</p>
                <p style="margin:0 0 16px;color:%s;">Welcome to <strong>%s</strong> — %s! Your account has been verified successfully.</p>
                <p style="margin:0 0 24px;color:%s;">Explore authentic Maheshwari handloom textiles woven on the banks of the Narmada.</p>
                """.formatted(TEXT, escape(name), TEXT, AppConstants.BRAND_NAME, AppConstants.BRAND_TAGLINE, TEXT),
                "Shop Now",
                AppConstants.BRAND_WEBSITE_URL + "/products"
        );
    }

    public String sellerRegistrationReceived(String businessName) {
        return layout(
                "Seller Registration Received",
                """
                <p style="margin:0 0 16px;color:%s;">Dear %s,</p>
                <p style="margin:0 0 16px;color:%s;">Thank you for registering as a seller on %s.</p>
                <p style="margin:0 0 16px;color:%s;">Your seller account has been registered successfully and is <strong>awaiting admin approval</strong>. Our team will review your details and notify you once approved.</p>
                <p style="margin:0;color:%s;font-size:14px;">You will not be able to log in to the seller dashboard until your account is approved.</p>
                """.formatted(TEXT, escape(businessName), TEXT, AppConstants.BRAND_NAME, TEXT, TEXT),
                null,
                null
        );
    }

    public String adminNewSeller(String ownerName, String businessName, String registrationDate, String sellerUrl) {
        return layout(
                "New Seller Registration",
                """
                <p style="margin:0 0 16px;color:%s;">A new seller has registered and is waiting for approval.</p>
                <table style="width:100%%;border-collapse:collapse;margin:16px 0;">
                  <tr><td style="padding:8px 0;color:%s;font-weight:600;width:140px;">Seller Name</td><td style="padding:8px 0;color:%s;">%s</td></tr>
                  <tr><td style="padding:8px 0;color:%s;font-weight:600;">Business Name</td><td style="padding:8px 0;color:%s;">%s</td></tr>
                  <tr><td style="padding:8px 0;color:%s;font-weight:600;">Registration Date</td><td style="padding:8px 0;color:%s;">%s</td></tr>
                </table>
                """.formatted(TEXT, TEXT, TEXT, escape(ownerName), TEXT, TEXT, escape(businessName), TEXT, TEXT, escape(registrationDate)),
                "View Seller",
                sellerUrl
        );
    }

    public String sellerApproved(String businessName) {
        return layout(
                "Seller Account Approved",
                """
                <p style="margin:0 0 16px;color:%s;">Dear %s,</p>
                <p style="margin:0 0 16px;color:%s;">Congratulations! Your seller account has been approved.</p>
                <p style="margin:0 0 24px;color:%s;">You can now log in and start adding products on the Maahish marketplace.</p>
                """.formatted(TEXT, escape(businessName), TEXT, TEXT),
                "Go to Seller Dashboard",
                AppConstants.BRAND_WEBSITE_URL + "/seller"
        );
    }

    public String sellerRejected(String businessName, String reason) {
        return layout(
                "Seller Registration Update",
                """
                <p style="margin:0 0 16px;color:%s;">Dear %s,</p>
                <p style="margin:0 0 16px;color:%s;">We regret to inform you that your seller registration on %s could not be approved at this time.</p>
                <p style="margin:0 0 16px;color:%s;"><strong>Reason:</strong> %s</p>
                <p style="margin:0;color:%s;">If you believe this is an error, contact us at %s or %s.</p>
                """.formatted(TEXT, escape(businessName), TEXT, AppConstants.BRAND_NAME, TEXT,
                        escape(reason != null ? reason : "Not specified"), TEXT,
                        AppConstants.SUPPORT_EMAIL, AppConstants.SUPPORT_PHONE),
                null,
                null
        );
    }

    public String sellerDeactivated(String businessName, String statusLabel, String reason) {
        String reasonBlock = StringUtils.hasText(reason)
                ? "<p style=\"margin:0 0 16px;color:" + TEXT + ";\"><strong>Reason:</strong> " + escape(reason) + "</p>"
                : "";
        return layout(
                "Seller Account " + capitalize(statusLabel),
                """
                <p style="margin:0 0 16px;color:%s;">Dear %s,</p>
                <p style="margin:0 0 16px;color:%s;">Your %s seller account has been <strong>%s</strong>.</p>
                %s
                <p style="margin:0;color:%s;">You cannot access the seller dashboard until your account is reactivated. Contact %s for assistance.</p>
                """.formatted(TEXT, escape(businessName), TEXT, AppConstants.BRAND_NAME, escape(statusLabel), reasonBlock, TEXT, AppConstants.SUPPORT_EMAIL),
                null,
                null
        );
    }

    public String newOrder(String recipientName, String orderNumber, String customerName, String sellerNames,
                           String orderValue, String orderDate, String actionUrl) {
        return layout(
                "New Order Placed",
                """
                <p style="margin:0 0 16px;color:%s;">Dear %s,</p>
                <p style="margin:0 0 16px;color:%s;">A new order has been placed on %s.</p>
                <table style="width:100%%;border-collapse:collapse;margin:16px 0;">
                  <tr><td style="padding:8px 0;color:%s;font-weight:600;width:140px;">Order ID</td><td style="padding:8px 0;color:%s;">%s</td></tr>
                  <tr><td style="padding:8px 0;color:%s;font-weight:600;">Customer</td><td style="padding:8px 0;color:%s;">%s</td></tr>
                  <tr><td style="padding:8px 0;color:%s;font-weight:600;">Seller</td><td style="padding:8px 0;color:%s;">%s</td></tr>
                  <tr><td style="padding:8px 0;color:%s;font-weight:600;">Order Value</td><td style="padding:8px 0;color:%s;">Rs %s</td></tr>
                  <tr><td style="padding:8px 0;color:%s;font-weight:600;">Order Date</td><td style="padding:8px 0;color:%s;">%s</td></tr>
                </table>
                """.formatted(TEXT, escape(recipientName), TEXT, AppConstants.BRAND_NAME,
                        TEXT, TEXT, escape(orderNumber), TEXT, TEXT, escape(customerName),
                        TEXT, TEXT, escape(sellerNames), TEXT, TEXT, escape(orderValue),
                        TEXT, TEXT, escape(orderDate)),
                "View Order",
                actionUrl
        );
    }

    public String orderConfirmed(String customerName, String orderNumber) {
        return layout(
                "Order Confirmed",
                """
                <p style="margin:0 0 16px;color:%s;">Dear %s,</p>
                <p style="margin:0 0 16px;color:%s;">Your order <strong>%s</strong> has been confirmed by the seller and is being prepared.</p>
                <p style="margin:0;color:%s;">We will notify you when your order is shipped.</p>
                """.formatted(TEXT, escape(customerName), TEXT, escape(orderNumber), TEXT),
                "Track Order",
                AppConstants.BRAND_WEBSITE_URL + "/orders/" + orderNumber
        );
    }

    public String orderDelivered(String customerName, String orderNumber) {
        return layout(
                "Order Delivered",
                """
                <p style="margin:0 0 16px;color:%s;">Dear %s,</p>
                <p style="margin:0 0 16px;color:%s;">Your order <strong>%s</strong> has been delivered successfully.</p>
                <p style="margin:0 0 24px;color:%s;">Thank you for shopping with %s. We hope you love your Maheshwari handloom purchase!</p>
                """.formatted(TEXT, escape(customerName), TEXT, escape(orderNumber), TEXT, AppConstants.BRAND_NAME),
                "View Order",
                AppConstants.BRAND_WEBSITE_URL + "/orders/" + orderNumber
        );
    }

    public String genericNotification(String title, String body) {
        return layout(title, "<p style=\"margin:0;color:" + TEXT + ";\">" + escape(body).replace("\n", "<br/>") + "</p>", null, null);
    }

    private String layout(String title, String bodyHtml, String ctaText, String ctaUrl) {
        String ctaBlock = "";
        if (StringUtils.hasText(ctaText) && StringUtils.hasText(ctaUrl)) {
            ctaBlock = """
                    <div style="text-align:center;margin:28px 0 8px;">
                      <a href="%s" style="display:inline-block;background:%s;color:#ffffff;text-decoration:none;padding:14px 28px;border-radius:8px;font-weight:600;font-size:15px;">%s</a>
                    </div>
                    """.formatted(escapeAttr(ctaUrl), MAROON, escape(ctaText));
        }

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;padding:0;background:#f5efe8;font-family:Georgia,'Times New Roman',serif;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f5efe8;padding:32px 16px;">
                    <tr><td align="center">
                      <table role="presentation" width="600" cellspacing="0" cellpadding="0" style="max-width:600px;width:100%%;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(91,31,58,0.12);">
                        <tr>
                          <td style="background:linear-gradient(135deg,%s 0%%,#3D1530 100%%);padding:28px 32px;text-align:center;">
                            <div style="font-size:28px;font-weight:700;color:#ffffff;letter-spacing:1px;">%s</div>
                            <div style="font-size:12px;color:%s;letter-spacing:2px;margin-top:6px;text-transform:uppercase;">%s</div>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:32px;">
                            <h1 style="margin:0 0 20px;font-size:22px;color:%s;font-weight:600;">%s</h1>
                            %s
                            %s
                          </td>
                        </tr>
                        <tr>
                          <td style="background:%s;padding:24px 32px;text-align:center;border-top:1px solid #eee;">
                            <p style="margin:0 0 8px;font-size:13px;color:%s;">%s</p>
                            <p style="margin:0;font-size:12px;color:#888;">%s · %s · %s</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                MAROON, AppConstants.BRAND_NAME, GOLD, AppConstants.BRAND_TAGLINE,
                MAROON, escape(title), bodyHtml, ctaBlock,
                IVORY, TEXT, AppConstants.BRAND_ADDRESS,
                AppConstants.SUPPORT_EMAIL, AppConstants.SUPPORT_PHONE, AppConstants.BRAND_WEBSITE_URL
        );
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String escapeAttr(String value) {
        return escape(value);
    }

    private String capitalize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
