package com.peng.certrecognition.service;

import com.peng.certrecognition.configuration.Constants;
import com.peng.certrecognition.domain.User;
import com.peng.certrecognition.repository.UserRepository;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class UserService extends BaseService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 根据用户邮箱获得用户信息
     * @param email
     * @return
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 根据用户邮箱和用户密码获得用户信息
     * @param email
     * @param password
     * @return
     */
    public User getUserByEmailAndPassword(String email, String password) {
        User user = getUserByEmail(email);
        if (user != null && user.isActive() && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * 注册用户
     * @param email
     * @param password
     * @return
     */
    public User addUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setActive(true);
        user.setCreateTime(new Date());
        return userRepository.save(user);
    }

    /**
     * 修改用户信息
     * @param user
     * @param key
     * @param value
     * @return
     */
    public User updateUser(User user, String key, String value) {
        if (key.equals(Constants.USER_KEY_USERNAME)) {
            user.setUsername(value);
        }
        if (key.equals(Constants.USER_KEY_EMAIL)) {
            user.setEmail(value);
        }
        if (key.equals(Constants.USER_KEY_PASSWORD)) {
            user.setPassword(value);
        }
        if (key.equals(Constants.USER_KEY_PHONE)) {
            user.setPhone(value);
        }
        if (key.equals(Constants.USER_KEY_SEX)) {
            user.setSex(value);
        }
        if (key.equals(Constants.USER_KEY_AVATAR)) {
            user.setAvatar(value);
        }
        user.setUpdateTime(new Date());
        return userRepository.save(user);
    }

    /**
     * 根据用户主键获得用户信息
     * @param userId
     * @return
     */
    public User getUserById(String userId) {
        return userRepository.findById(userId);
    }

    /**
     * 从cookie中获得用户id，再根据id从数据库中取得用户信息
     * @return
     */
    public User getUserByCookie() {
        String userId = getUseridByCookie();
        return (userId != null) ? getUserById(userId) : null;
    }

    /**
     * 从cookie中获得用户id
     * @return
     */
    public String getUseridByCookie() {
        return redisTemplate.opsForValue().get(Constants.TOKEN_PREFIX + getUserToken());
    }

    /**
     * 生成用户token
     * @param user
     * @return
     */
    public String genToken(User user) {
        SecureRandom random = new SecureRandom();
        byte[] values = new byte[20];
        random.nextBytes(values);
        String token = Hex.encodeHexString(values);
        redisTemplate.opsForValue().set(Constants.TOKEN_PREFIX + token, user.getId(), 1, TimeUnit.DAYS);
        return token;
    }

    /**
     * 判断token是否有效
     * @param token
     * @return
     */
    public boolean isValidToken(String token) {
        return redisTemplate.opsForValue().get(Constants.TOKEN_PREFIX + token) != null;
    }

    /**
     * 获得用户token
     * @return
     */
    public String getUserToken() {
        HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return curRequest.getHeader("Authorization");
    }

    /**
     * 从cookies中获得用户token
     * @return
     */
    public String getUserTokenFromCookies() {
        HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Cookie[] cookies = curRequest.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : curRequest.getCookies()) {
            if (cookie.getName().equals("token")) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 删除用户token
     */
    public void removeToken() {
        String token = getUserToken();
        if (token != null) {
            redisTemplate.delete(token);
        }
    }

}
