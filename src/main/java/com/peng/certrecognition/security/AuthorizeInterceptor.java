package com.peng.certrecognition.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peng.certrecognition.configuration.Constants;
import com.peng.certrecognition.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class AuthorizeInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = userService.getUserToken();
        if (token == null || !userService.isValidToken(token)) {
            for (String key : Constants.RESPONSE_HEADER.keySet()) {
                response.addHeader(key, Constants.RESPONSE_HEADER.getFirst(key));
            }
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_OK);
                return true;
            } else {
                Map<String, Object> errorMap = new HashMap<>();
                ObjectMapper mapper = new ObjectMapper();
                errorMap.put("success", false);
                errorMap.put("message", "User token expired.");
                String errorStr = mapper.writeValueAsString(errorMap);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(errorStr);
                return false;
            }
        }
        return true;
    }
}
