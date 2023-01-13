/*
 * Copyright (c) 2023-present KuFlow S.L.
 *
 * All rights reserved.
 */
package com.kuflow.engine.samples.worker;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;

@SpringBootApplication
@EnableConfigurationProperties({ SampleEngineWorkerUiVisionProperties.class })
public class SampleEngineWorkerUiVisionApp implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleEngineWorkerUiVisionApp.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SampleEngineWorkerUiVisionApp.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    @Override
    public void run(String... args) {
        LOGGER.info("Running...");
    }

    private static void logApplicationStartup(Environment env) {
        String[] profiles = ArrayUtils.isNotEmpty(env.getActiveProfiles()) ? env.getActiveProfiles() : env.getDefaultProfiles();
        LOGGER.info(
            """

            ----------------------------------------------------------
            \tApplication '{}' is running!
            \tProfile(s): \t{}
            ----------------------------------------------------------
            """,
            env.getProperty("spring.application.name"),
            profiles
        );
    }
}