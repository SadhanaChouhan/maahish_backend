package com.maahish.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.maahish.config.SmsProperties;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsProperties smsProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void send(String mobile, String message) {
        if (!StringUtils.hasText(mobile) || !StringUtils.hasText(message)) {
            log.warn("SMS skipped: mobile or message is empty");
            return;
        }

        String normalizedMobile = normalizeMobile(mobile);
        if (normalizedMobile == null) {
            log.warn("SMS skipped: invalid mobile number {}", mobile);
            return;
        }

        if (!smsProperties.isEnabled() || "console".equalsIgnoreCase(smsProperties.getProvider())) {
            log.info("""
                    
                    ========== Maahish SMS ==========
                    To: +{} {}
                    Message: {}
                    ================================
                    """, smsProperties.getCountryCode(), normalizedMobile, message);
            return;
        }

        try {
            String provider = smsProperties.getProvider().toLowerCase();
            if ("fast2sms".equals(provider)) {
                sendViaFast2Sms(normalizedMobile, message);
            } else if ("msg91".equals(provider)) {
                sendViaMsg91(normalizedMobile, message);
            } else {
                log.warn("Unknown SMS provider '{}'. Message logged only.", smsProperties.getProvider());
                log.info("SMS content for {}: {}", normalizedMobile, message);
            }
        } catch (Exception ex) {
            log.error("Failed to send SMS to {}: {}", normalizedMobile, ex.getMessage());
            log.info("SMS fallback content to {}: {}", normalizedMobile, message);
        }
    }

    private void sendViaFast2Sms(String mobile, String message) {
        if (!StringUtils.hasText(smsProperties.getApiKey())) {
            throw new IllegalStateException("Fast2SMS API key is not configured");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("authorization", smsProperties.getApiKey().trim());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "route", "q",
                "message", message,
                "language", "english",
                "numbers", mobile
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://www.fast2sms.com/dev/bulkV2",
                new HttpEntity<>(body, headers),
                String.class
        );
        log.info("Fast2SMS response for {}: {}", mobile, response.getBody());
    }

    private void sendViaMsg91(String mobile, String message) {
        if (!StringUtils.hasText(smsProperties.getApiKey())) {
            throw new IllegalStateException("MSG91 auth key is not configured");
        }

        URI uri = UriComponentsBuilder
                .fromHttpUrl("https://api.msg91.com/api/sendhttp.php")
                .queryParam("authkey", smsProperties.getApiKey().trim())
                .queryParam("mobiles", mobile)
                .queryParam("message", message)
                .queryParam("sender", smsProperties.getSenderId())
                .queryParam("route", "4")
                .queryParam("country", smsProperties.getCountryCode())
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        log.info("MSG91 response for {}: {}", mobile, response.getBody());
    }

    private String normalizeMobile(String mobile) {
        String digits = mobile.replaceAll("\\D", "");
        if (digits.length() == 12 && digits.startsWith("91")) {
            digits = digits.substring(2);
        }
        if (digits.length() == 10 && digits.matches("^[6-9]\\d{9}$")) {
            return digits;
        }
        return null;
    }
}
