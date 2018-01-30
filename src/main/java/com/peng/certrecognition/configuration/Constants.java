package com.peng.certrecognition.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public final class Constants {

    public static final String TOKEN_PREFIX = "auto-report:token:";
    public static final String DATASOURCE_QUEUE_KEY = "auto-report:datasource:queue";
    public static final String REPORT_QUEUE_KEY = "auto-report:report:queue";
    public static String IMAGE_FORMAT_URL;
    public static HttpHeaders RESPONSE_HEADER = new HttpHeaders();

    static {
        RESPONSE_HEADER.add("Access-Control-Allow-Origin", "*");
        RESPONSE_HEADER.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        RESPONSE_HEADER.add("Access-Control-Allow-Credentials", "true");
        RESPONSE_HEADER.add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        RESPONSE_HEADER.add("Content-Type", "application/json");
    }

    @Value("${imageformat.url}")
    public void setImageFormatUrl(String imageFormatUrl){
        IMAGE_FORMAT_URL = imageFormatUrl;
    }

}
