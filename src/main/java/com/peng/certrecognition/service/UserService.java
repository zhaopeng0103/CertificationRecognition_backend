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
import java.util.concurrent.TimeUnit;

@Service
public class UserService extends BaseService {

    @Autowired
    private UserRepository userRepository;

    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    public User getUserByEmailAndPassword(String email, String password) {
        User user = userRepository.findUserByEmail(email);
        if (user != null && user.isActive() && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public User createUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        userRepository.save(user);
        return user;
    }

    public User getUserById(String userId) {
        return userRepository.findUserById(userId);
    }

    public User getUserByCookie() {
        String userId = redisTemplate.opsForValue().get(Constants.TOKEN_PREFIX + getUserToken());
        if (userId != null) {
            return getUserById(userId);
        }
        return null;
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

}
