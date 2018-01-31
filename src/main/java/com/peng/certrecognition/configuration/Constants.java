package com.peng.certrecognition.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public final class Constants {

    public static final String TOKEN_PREFIX = "CertificationRecognition_backend:token:";
    public static String PHOTO_PATH;
    public static HttpHeaders RESPONSE_HEADER = new HttpHeaders();

    static {
        RESPONSE_HEADER.add("Access-Control-Allow-Origin", "*");
        RESPONSE_HEADER.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        RESPONSE_HEADER.add("Access-Control-Allow-Credentials", "true");
        RESPONSE_HEADER.add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        RESPONSE_HEADER.add("Content-Type", "application/json");
    }

    @Value("${photo.path}")
    public void setPhotoPath(String photoPath){
        PHOTO_PATH = photoPath;
    }

}
