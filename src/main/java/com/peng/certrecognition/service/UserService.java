package com.abcft.report.service;

import com.abcft.report.configuration.Constants;
import com.abcft.report.domain.User;
import com.abcft.report.domain.UserRepository;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserService extends BaseService {

    @Autowired
    public UserRepository userRepository;

    @Autowired
    ReportService ownCloudService;

    public User getUserByEmail(String userEmail) {
        return userRepository.findUserByEmail(userEmail);
    }

    public User getUserById(String userId) {
        return userRepository.findUserById(userId);
    }

    public User getUserByEmailAndPassword(String email, String password) {
        User user = userRepository.findUserByEmail(email);
        if (user != null && user.isActive() && user.isValidPassword(password)) {
            return user;
        }
        return null;
    }

    public User getUserByCookie() {
        String userId = redisTemplate.opsForValue().get(Constants.TOKEN_PREFIX + getUserToken());
        if (userId != null) {
            return getUserById(userId);
        }
        return null;
    }

    public User createUser(String email, String password) {
        User user = new User(email);
        user.setPassword(password);
        user.setOwnCloudPassword(UUID.randomUUID().toString());
        userRepository.refreshOwnCloudToken(user);
        userRepository.save(user);
        return user;
    }

    public String genToken(User user) {
        SecureRandom random = new SecureRandom();
        byte[] values = new byte[20];
        random.nextBytes(values);
        String token = Hex.encodeHexString(values);
        redisTemplate.opsForValue().set(Constants.TOKEN_PREFIX + token, user.getId(), 1, TimeUnit.DAYS);
        return token;
    }

    public boolean isValidToken(String token) {
        return redisTemplate.opsForValue().get(Constants.TOKEN_PREFIX + token) != null;
    }

    public String getUserToken() {
        HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return curRequest.getHeader("Authorization");
    }

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

    public void removeToken() {
        String token = getUserToken();
        if (token != null) {
            redisTemplate.delete(token);
        }
    }

    public boolean isValidOwnCloudUser(String email, String password) {
        User user = userRepository.findUserByEmail(email);
        return user.isActive() && user.isValidOwnCloudPassword(password);
    }
}
