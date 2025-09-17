package com.back.global.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class AppConfig {
    private static Environment environment;

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
}
