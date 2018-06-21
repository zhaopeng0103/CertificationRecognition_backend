package com.peng.certrecognition.controller;

import com.peng.certrecognition.configuration.Constants;
import com.peng.certrecognition.domain.User;
import com.peng.certrecognition.service.UserService;
import com.peng.certrecognition.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/user/login", method = RequestMethod.POST)
    public ResponseEntity<?> userLogin(@RequestParam("email") String email, @RequestParam("password") String password) {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        User user = userService.getUserByEmail(email);
        if (user == null) {
            logger.info("UserController::userLogin:failed:{}", "邮箱账户不存在！");
            return responseError(methodName, "邮箱账户不存在！");
        } else {
            user = userService.getUserByEmailAndPassword(email, password);
            if (user == null) {
                logger.info("UserController::userLogin:failed:{}", "账户密码不正确！");
                return responseError(methodName, "账户密码不正确！");
            } else {
                String token = userService.genToken(user);
                Map<String, Object> data = new HashMap<>();
                data.put("userId", user.getId());
                data.put("email", user.getEmail());
                data.put("token", token);
                logger.info("UserController::userLogin:finished:{}", data);
                return responseSuccess(methodName, data, "登录成功！");
            }
        }
    }

    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    public ResponseEntity<?> userRegister(@RequestParam("username") String username, @RequestParam("email") String email, @RequestParam("password") String password) {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        User user = userService.getUserByEmail(email);
        if (user == null) {
            user = userService.addUser(username, email, password);
            Map<String, Object> data = user.toMapFromRegister();
            logger.info("UserController::userRegister:finished:{}", data);
            return responseSuccess(methodName, data, "注册成功！");
        } else {
            logger.info("UserController::userRegister:failed:{}", "邮箱账户已存在！");
            return responseError(methodName, "邮箱账户已存在！");
        }
    }

    @RequestMapping(value = "/user/logout", method = RequestMethod.GET)
    public ResponseEntity<?> userLogout() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        userService.removeToken();
        logger.info("UserController::userLogout:finished:{}", "登出成功！");
        return responseSuccess(methodName, "登出成功！");
    }

    @RequestMapping(value = "/user/info", method = RequestMethod.GET)
    public ResponseEntity<?> userInfo() {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        User user = getCurrentUser();
        if (user != null) {
            Map<String, Object> data = user.toMapFromInfo();
            logger.info("UserController::userInfo:finished:{}", data);
            return responseSuccess(methodName, data, "用户信息获得成功！");
        } else {
            logger.info("UserController::userInfo:failed:{}", "用户不存在！");
            return responseError(methodName, "用户不存在！");
        }
    }

    @RequestMapping(value = "/user/update", method = RequestMethod.POST)
    public ResponseEntity<?> userUpdate(@RequestParam("key") String key, @RequestParam("value") String value) {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        User user = getCurrentUser();
        if (user != null) {
            user = userService.updateUser(user, key, value);
            Map<String, Object> data = user.toMapFromUpdate();
            logger.info("UserController::userUpdate:finished:{}", data);
            return responseSuccess(methodName, data, "修改成功！");
        } else {
            logger.info("UserController::userUpdate:failed:{}", "用户不存在！");
            return responseError(methodName, "用户不存在！");
        }
    }

    @RequestMapping(value = "/head/upload", method = RequestMethod.POST)
    public ResponseEntity<?> headUpload(@RequestParam(value = "file") MultipartFile file) {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        User user = getCurrentUser();
        if (user != null) {
            if (file.isEmpty()) {
                logger.info("UserController::headUpload:failed:{}", "图片不存在！");
                return responseError(methodName, "图片不存在！");
            }
            String fileName = file.getOriginalFilename();
            File dest = new File(Constants.HEAD_PATH.concat(user.getEmail()).concat("/"), fileName);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            try {
                file.transferTo(dest);
                user = userService.updateUser(user, Constants.USER_KEY_AVATAR, dest.getAbsolutePath());
                Map<String, Object> data = new HashMap<>();
                data.put("avatar", user.getAvatar());
                data.put("update_time", DateUtils.getTransformDate(user.getUpdateTime()));
                logger.info("UserController::headUpload:success:{}", "头像上传完成！");
                return responseSuccess(methodName, data, "头像上传完成");
            } catch (IOException e) {
                logger.error("UserController::headUpload:failed:{}", "头像上传失败！", e.getMessage());
                return responseError(methodName, "头像上传失败！");
            }
        } else {
            logger.info("UserController::headUpload:failed:{}", "用户不存在！");
            return responseError(methodName, "用户不存在！");
        }
    }

    @RequestMapping(value = "/head/download", method = RequestMethod.GET)
    public ResponseEntity<?> headDownload(HttpServletResponse response) {
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        User user = getCurrentUser();
        if (user != null) {
            File file = new File(user.getAvatar());
            if (file.exists()) {
                response.setContentType("application/force-download");
                response.addHeader("Content-Disposition", "attachment;fileName=" + file.getName());
                byte[] buffer = new byte[1024];
                try {
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                    OutputStream outputStream = response.getOutputStream();
                    int i = bufferedInputStream.read(buffer);
                    while (i != -1) {
                        outputStream.write(buffer, 0, i);
                        i = bufferedInputStream.read(buffer);
                    }
                    bufferedInputStream.close();
                    logger.info("UserController::headDownload:success:{}", "头像下载完成！");
                    return responseSuccess(methodName, "头像下载完成");
                } catch (Exception e) {
                    logger.error("UserController::headDownload:failed:{}", "头像下载失败！", e.getMessage());
                    return responseError(methodName, "头像下载失败！");
                }
            } else {
                logger.info("UserController::headDownload:failed:{}", "头像不存在！");
                return responseError(methodName, "头像不存在！");
            }
        } else {
            logger.info("UserController::headDownload:failed:{}", "用户不存在！");
            return responseError(methodName, "用户不存在！");
        }
    }

}
