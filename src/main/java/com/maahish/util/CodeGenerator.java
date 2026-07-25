package com.maahish.util;

import com.maahish.constants.AppConstants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public final class CodeGenerator {

    private static final AtomicLong PRODUCT_SEQ = new AtomicLong(1000);

    private CodeGenerator() {}

    public static String generateProductCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMM"));
        return AppConstants.PRODUCT_CODE_PREFIX + timestamp + PRODUCT_SEQ.incrementAndGet();
    }
}
