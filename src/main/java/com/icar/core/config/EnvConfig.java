package com.icar.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads configuration for the selected environment.
 * Select env with -Denv=qa (or ENV variable). Default: dev.
 * Any property can be overridden by a system property (-Dbase.url=...).
 * Secrets (token) are NEVER stored in the repo: ICAR_API_TOKEN env variable.
 */
public final class EnvConfig {

    private static final String DEFAULT_ENV = "dev";
    private static final Properties PROPS = load();

    private EnvConfig() {
    }

    public static String envName() {
        String env = System.getProperty("env", System.getenv("ENV"));
        return (env == null || env.isBlank()) ? DEFAULT_ENV : env.toLowerCase();
    }

    public static String baseUrl() {
        return get("base.url");
    }

    public static int timeoutMs() {
        return Integer.parseInt(get("timeout.ms"));
    }

    /** Optional: returns null when token is not provided. */
    public static String apiToken() {
        String token = System.getenv("ICAR_API_TOKEN");
        return (token == null || token.isBlank()) ? null : token;
    }

    private static String get(String key) {
        return System.getProperty(key, PROPS.getProperty(key));
    }

    private static Properties load() {
        String file = "config/" + envName() + ".properties";
        try (InputStream in = EnvConfig.class.getClassLoader().getResourceAsStream(file)) {
            if (in == null) {
                throw new IllegalStateException("Config file not found: " + file);
            }
            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read config: " + file, e);
        }
    }
}
