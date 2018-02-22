package com.peng.certrecognition.controller;

import com.peng.certrecognition.domain.User;
import com.peng.certrecognition.service.UserService;
import com.peng.certrecognition.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/user/login", method = RequestMethod.POST)
    public ResponseEntity<?> login(@RequestParam("email") String email, @RequestParam("password") String password) {
        User user = userService.getUserByEmail(email);
        if (user == null){
            logger.info("UserController::userLogin:failed:{}", "邮箱账户不存在！");
            return responseError("邮箱账户不存在！");
        }else{
            user = userService.getUserByEmailAndPassword(email, password);
            if (user == null) {
                logger.info("UserController::userLogin:failed:{}", "账户密码不正确！");
                return responseError("账户密码不正确！");
            }else{
                String token = userService.genToken(user);
                Map<String, Object> data = new HashMap<>();
                data.put("userId", user.getId());
                data.put("email", user.getEmail());
                data.put("token", token);
                logger.info("UserController::userLogin:finished:{}", data);
                return responseSuccess(data, "登录成功！");
            }
        }
    }

    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    public ResponseEntity<?> register(@RequestParam("email") String email, @RequestParam("password") String password) {
        User user = userService.getUserByEmail(email);
        if (user == null){
            user = userService.addUser(email, password);
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("email", user.getEmail());
            data.put("create_time", user.getCreateTime());
            logger.info("UserController::userRegister:finished:{}", data);
            return responseSuccess(data, "注册成功！");
        } else {
            logger.info("UserController::userRegister:failed:{}", "邮箱账户已存在！");
            return responseError("邮箱账户已存在！");
        }
    }

    @RequestMapping(value = "/user/logout", method = RequestMethod.GET)
    public ResponseEntity<?> logout() {
        userService.removeToken();
        logger.info("UserController::userLogout:finished:{}", "登出成功！");
        return responseSuccess("登出成功！");
    }

    @RequestMapping(value = "/user/info", method = RequestMethod.GET)
    public ResponseEntity<?> info() {
        User user = getCurrentUser();
        if (user != null){
            Map<String, Object> data = new HashMap<>();
            data.put("userid", user.getId());
            data.put("username", user.getUsername());
            data.put("avatar", user.getAvatar());
            data.put("email", user.getEmail());
            data.put("phone", user.getPhone());
            data.put("sex", user.getSex());
            data.put("create_time", DateUtils.getTransformDate(user.getCreateTime()));
            logger.info("UserController::userInfo:finished:{}", data);
            return responseSuccess(data, "用户信息获得成功！");
        } else {
            logger.info("UserController::userInfo:failed:{}", "用户不存在！");
            return responseError("用户不存在！");
        }
    }

    @RequestMapping(value = "/user/update", method = RequestMethod.POST)
    public ResponseEntity<?> update(@RequestParam("key") String key, @RequestParam("value") String value) {
        User user = getCurrentUser();
        if (user != null) {
            user = userService.updateUser(user, key, value);
            Map<String, Object> data = new HashMap<>();
            data.put("userid", user.getId());
            data.put("email", user.getEmail());
            data.put("username", user.getUsername());
            data.put("avatar", user.getAvatar());
            data.put("phone", user.getPhone());
            data.put("sex", user.getSex());
            data.put("update_time", user.getUpdateTime());
            logger.info("UserController::userUpdate:finished:{}", data);
            return responseSuccess(data, "修改成功！");
        } else {
            logger.info("UserController::userUpdate:failed:{}", "用户不存在！");
            return responseError("用户不存在！");
        }
    }

}
