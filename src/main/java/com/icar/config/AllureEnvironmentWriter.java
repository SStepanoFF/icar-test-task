package com.icar.config;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Writes environment.properties into the Allure results directory,
 * so the report shows which environment the tests ran against.
 * Never writes secrets (no token).
 */
@Slf4j
public final class AllureEnvironmentWriter {

    private static boolean written;

    private AllureEnvironmentWriter() {
    }

    public static synchronized void write() {
        if (written) {
            return;
        }
        Path dir = Paths.get(System.getProperty("allure.results.directory", "target/allure-results"));
        Properties env = new Properties();
        env.setProperty("Environment", EnvConfig.envName());
        env.setProperty("Base.URL", EnvConfig.baseUrl());

        try {
            Files.createDirectories(dir);
            try (OutputStream out = Files.newOutputStream(dir.resolve("environment.properties"))) {
                env.store(out, "Allure environment");
            }
            written = true;
        } catch (IOException e) {
            log.warn("Could not write Allure environment.properties: {}", e.getMessage());
        }
    }
}
