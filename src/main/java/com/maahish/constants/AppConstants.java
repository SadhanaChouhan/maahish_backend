package com.maahish.constants;

public final class AppConstants {

    private AppConstants() {}

    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIR = "desc";

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final String PRODUCT_CODE_PREFIX = "MHS";
    public static final String ORDER_NUMBER_PREFIX = "ORD";

    public static final String[] ADMIN_URLS = {
            "/v1/admin/**"
    };

    public static final String[] SELLER_URLS = {
            "/v1/seller/**"
    };

    public static final String PLATFORM_SELLER_EMAIL = "platform@maahish.com";

    public static final String CUSTOMER_PURCHASE_DENIED_MESSAGE =
            "Only customers can purchase products.";
}
