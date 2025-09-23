package com.back.global.app;

import com.back.standard.util.Ut;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {
    private static Environment environment;

    private static ObjectMapper objectMapper;

    @Autowired
    public void setEnvironment(Environment environment) {
        AppConfig.environment = environment;
    }

    public static boolean isDev() {
        return environment.matchesProfiles("dev");
    }

    public static boolean isTest() {
        return environment.matchesProfiles("test");
    }


    public static boolean isProd() {
        return environment.matchesProfiles("prod");
    }

    public static boolean isNotProd() {
        return !isProd();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        AppConfig.objectMapper = objectMapper;
    }

    @PostConstruct // 빈(Bean)이 생성되고 의존성 주입이 끝난 직후에 딱 한 번 호출되는 초기화 훅(hook)
    public void postConstruct() {
        Ut.json.objectMapper = objectMapper;
    }
}
