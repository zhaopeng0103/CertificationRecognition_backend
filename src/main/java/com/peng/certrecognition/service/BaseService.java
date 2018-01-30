package com.peng.certrecognition.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

public class BaseService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RedisTemplate<String, String> redisTemplate;

}
