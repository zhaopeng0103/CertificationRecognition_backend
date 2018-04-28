package com.peng.certrecognition.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public final class Constants {

    public static final String USER_KEY_EMAIL = "email";
    public static final String USER_KEY_PASSWORD = "password";
    public static final String USER_KEY_AVATAR = "avatar";
    public static final String USER_KEY_USERNAME = "username";
    public static final String USER_KEY_PHONE = "phone";
    public static final String USER_KEY_SEX = "sex";
    public static final String USER_KEY_BIRTHDAY = "birthday";

    public static final String RECOG_TITLE = "title";
    public static final String RECOG_NAME = "name";
    public static final String RECOG_SITUATION = "situation";
    public static final String RECOG_LOCATION = "location";
    public static final String RECOG_NUMBER = "number";
    public static final String RECOG_RIGHTTYPE = "rightType";
    public static final String RECOG_RIGHTNATURE = "rightNature";
    public static final String RECOG_USING = "using";
    public static final String RECOG_AREA = "area";
    public static final String RECOG_TIMELIMIT = "timeLimit";
    public static final String RECOG_OTHER = "other";
    public static final String RECOG_ADDIN = "addin";
    public static final String WORD_NAME = "word_name";
    public static final String WORD = "word";

    public static final String TOKEN_PREFIX = "CertificationRecognition_backend:token:";
    public static String HEAD_PATH;
    public static String PHOTO_PATH;
    public static String PYTHON_PATH;
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

}
