package com.abcft.report.controller;

import com.abcft.report.domain.User;
import com.abcft.report.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BaseController {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserService userService;

    ResponseEntity<?> response(Object response, HttpStatus httpStatus) {
        return new ResponseEntity<>(response, httpStatus);
    }

    ResponseEntity<?> responseSuccess(Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return response(response, HttpStatus.OK);
    }

    ResponseEntity<?> responseError(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response(response, HttpStatus.OK);
    }

    ResponseEntity<?> responseSuccess() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response(response, HttpStatus.OK);
    }

    ResponseEntity<?> responseSuccess(List<?> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("total", data.size());
        return response(response, HttpStatus.OK);
    }

    ResponseEntity<?> responseSuccess(InputStreamResource inputStreamResource, HttpHeaders httpHeaders) {
        return new ResponseEntity<>(inputStreamResource, httpHeaders, HttpStatus.OK);
    }

    User getCurrentUser() {
        return userService.getUserByCookie();
    }
}
