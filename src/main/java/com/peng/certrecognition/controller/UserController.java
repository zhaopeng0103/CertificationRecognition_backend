package com.abcft.report.controller;

import com.abcft.report.domain.User;
import com.abcft.report.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
public class UserController extends BaseController {

    @Autowired
    UserService userService;

    @RequestMapping(value = "/v1/user/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestParam("email") String email, @RequestParam("password") String password) {
        User user = userService.getUserByEmailAndPassword(email, password);
        if (user == null) {
            return responseError("Invalid user.");
        }
        String token = userService.genToken(user);
        Map<String, Object> data = new HashMap<>();
        data.put("uid", user.getId());
        data.put("token", token);
        logger.info("login:{}", email);
        return responseSuccess(data);
    }

    @RequestMapping(value = "/v1/user/create", method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestParam("email") String email, @RequestParam("password") String password) {
        userService.createUser(email, password);
        logger.info("UserController::userCreate:finished:{}", email);
        return responseSuccess();
    }

    @RequestMapping(value = "/v1/user/logout", method = RequestMethod.GET)
    public ResponseEntity<?> logout() {
        userService.removeToken();
        logger.info("UserController::userLogout:finished:{}");
        return responseSuccess();
    }

    @RequestMapping(value = "/v1/user/valid", method = RequestMethod.GET)
    public ResponseEntity<?> checkUser() {
        return responseSuccess();
    }
}
