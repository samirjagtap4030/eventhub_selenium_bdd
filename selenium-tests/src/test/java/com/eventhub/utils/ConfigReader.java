package com.eventhub.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads test configuration with a two-tier lookup:
 *
 * <ol>
 *   <li><b>Environment variable</b> — checked first so CI pipelines can inject
 *       secrets without modifying {@code config.properties}.
 *       Key {@code "user.password"} maps to env var {@code USER_PASSWORD}
 *       (upper-cased, dots replaced with underscores).</li>
 *   <li><b>config.properties</b> — fallback for local development.</li>
 * </ol>
 *
 * All values are loaded once at class-load time (thread-safe, immutable after init).
 */
public final class ConfigReader {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = ConfigReader.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new ExceptionInInitializerError(
                        "config.properties not found on classpath");
            }
            PROPS.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private ConfigReader() {}

    /**
     * Returns the value for {@code key}.
     * Env var takes precedence over config.properties so CI can override credentials
     * without touching source code.
     *
     * <pre>
     *   config key   →  env var
     *   user.email   →  USER_EMAIL
     *   user.password→  USER_PASSWORD
     *   base.url     →  BASE_URL
     * </pre>
     */
    public static String get(String key) {
        // 1. Check environment variable (dots → underscores, upper-case)
        String envKey   = key.toUpperCase().replace('.', '_');
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }

        // 2. Fall back to config.properties
        String value = PROPS.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException(
                    "Missing config key '" + key
                            + "' — not found in env var '" + envKey
                            + "' or config.properties");
        }
        return value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }
}
