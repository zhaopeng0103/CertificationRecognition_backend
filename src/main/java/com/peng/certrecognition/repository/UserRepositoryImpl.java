package com.abcft.report.domain;

import com.abcft.report.configuration.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public User refreshOwnCloudToken(User user) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        params.put("user", user.getEmail());
        params.put("password", user.getOwnCloudPassword());
        ResponseEntity<Map> response = restTemplate.postForEntity(Constants.OWNCLOUD_TOKEN_URI, params, Map.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            logger.error("OwnCloudService refresh owncloud token failed: {};", user.getEmail());
            return user;
        }
        user.setOwnCloudToken(response.getBody().get("token").toString());
        logger.info("OwnCloudService refresh owncloud token: {};", user.getEmail());
        return user;
    }
}
