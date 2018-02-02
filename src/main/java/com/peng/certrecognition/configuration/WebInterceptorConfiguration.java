package com.peng.certrecognition.configuration;

import com.peng.certrecognition.security.AuthorizeInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebInterceptorConfiguration extends WebMvcConfigurerAdapter {

    private String[] excludePathPatterns = new String[]{
            "/user/login",
            "/user/register",
    };

    @Bean
    public AuthorizeInterceptor authorizeInterceptor() {
        return new AuthorizeInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorizeInterceptor()).excludePathPatterns(excludePathPatterns);
        super.addInterceptors(registry);
    }

}
