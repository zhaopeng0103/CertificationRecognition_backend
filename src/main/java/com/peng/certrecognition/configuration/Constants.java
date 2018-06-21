package com.peng.certrecognition.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public final class Constants {
    public static final String APP_ID = "11408721";
    public static final String API_KEY = "RH0SbrjyfwiRpNMToWPPQ4Hd";
    public static final String SECRET_KEY = "et6Bwt1GUYoQNVhEXaHB63t6pfcd4je4";

    public static final String USER_KEY_EMAIL = "email";
    public static final String USER_KEY_PASSWORD = "password";
    public static final String USER_KEY_AVATAR = "avatar";
    public static final String USER_KEY_USERNAME = "username";
    public static final String USER_KEY_PHONE = "phone";
    public static final String USER_KEY_SEX = "sex";
    public static final String USER_KEY_BIRTHDAY = "birthday";

    public static final String IMG_ROTATE = "rotate";
    public static final String IMG_HOR_CORRECTION = "horizontal_correction";
    public static final String IMG_SOURCE = "source";
    public static final String IMG_GRAY = "gray";
    public static final String IMG_THRESH = "thresh";
    public static final String IMG_HORIZONTAL = "horizontal";
    public static final String IMG_VERTICAL = "vertical";
    public static final String IMG_MASK = "mask";
    public static final String IMG_JOINTS = "joints";
    public static final String IMG_ROIS = "rois";
    public static final String IMG_CELLS = "cells";

    public static final String WORD_NAME = "word_name";
    public static final String WORD = "word";

    public static final String TOKEN_PREFIX = "CertificationRecognition_backend:token:";
    public static String HEAD_PATH;
    public static String PHOTO_PATH;
    public static String PYTHON_PATH;
    public static String OCR_PATH;
    public static HttpHeaders RESPONSE_HEADER = new HttpHeaders();

    static {
        RESPONSE_HEADER.add("Access-Control-Allow-Origin", "*");
        RESPONSE_HEADER.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        RESPONSE_HEADER.add("Access-Control-Allow-Credentials", "true");
        RESPONSE_HEADER.add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        RESPONSE_HEADER.add("Content-Type", "application/json");
    }

    @Value("${head.path}")
    public void setHeadPath(String headPath) {
        HEAD_PATH = headPath;
    }

    @Value("${photo.path}")
    public void setPhotoPath(String photoPath) {
        PHOTO_PATH = photoPath;
    }

    @Value("${python.path}")
    public void setPythonPath(String pythonPath) {
        PYTHON_PATH = pythonPath;
    }

    @Value("${ocr.path}")
    public void setOcrPath(String ocrPath) {
        OCR_PATH = ocrPath;
    }

}
