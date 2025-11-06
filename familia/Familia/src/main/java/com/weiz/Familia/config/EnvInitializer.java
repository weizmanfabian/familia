package com.weiz.Familia.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class EnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(EnvInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            Map<String, Object> envProps = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                envProps.put(entry.getKey(), entry.getValue());

            });

            environment.getPropertySources().addFirst(new MapPropertySource("dotenv", envProps));
        } catch (Exception e) {
            log.error("Warning: Could not load .env file: {}", e.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
