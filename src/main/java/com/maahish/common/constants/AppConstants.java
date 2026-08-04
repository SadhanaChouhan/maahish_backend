package com.maahish.common.constants;

public final class AppConstants {

    private AppConstants() {}

    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIR = "desc";

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final String ACCESS_TOKEN_COOKIE = "maahish_access_token";
    public static final String REFRESH_TOKEN_COOKIE = "maahish_refresh_token";
    public static final String ACCESS_TOKEN_COOKIE_PATH = "/api";
    public static final String REFRESH_TOKEN_COOKIE_PATH = "/api/v1/auth";

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

    public static final String BRAND_NAME = "Maahish";
    public static final String BRAND_TAGLINE = "The Textile of Maheshwar";
    public static final String BRAND_DOMAIN = "themaahish.com";
    public static final String BRAND_WEBSITE_URL = "https://www.themaahish.com";
    public static final String SUPPORT_EMAIL = "support@themaahish.com";
    public static final String OFFICIAL_EMAIL = "themaahish@gmail.com";
    public static final String SUPPORT_PHONE = "+91 72239 59729";
    public static final String BRAND_ADDRESS = "Maheshwar, Madhya Pradesh, India 451224";
    public static final String YOUTUBE_URL = "https://www.youtube.com/@TheMaahish";
}
