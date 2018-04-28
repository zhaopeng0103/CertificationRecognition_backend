package com.peng.certrecognition.controller;

import com.peng.certrecognition.domain.User;
import com.peng.certrecognition.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BaseController {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    private ResponseEntity<?> response(Object response, HttpStatus httpStatus) {
        return new ResponseEntity<>(response, httpStatus);
    }

    ResponseEntity<?> responseSuccess(String method, Map<String, Object> data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("method", method);
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response(response, HttpStatus.OK);
    }

    ResponseEntity<?> responseError(String method, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("method", method);
        response.put("success", false);
        response.put("message", message);
        return response(response, HttpStatus.OK);
    }

    ResponseEntity<?> responseSuccess(String method, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("method", method);
        response.put("success", true);
        response.put("message", message);
        return response(response, HttpStatus.OK);
    }

    ResponseEntity<?> responseSuccess(String method, List<?> data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("method", method);
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        response.put("sizes", data.size());
        return response(response, HttpStatus.OK);
    }

    User getCurrentUser() {
        return userService.getUserByCookie();
    }

}
