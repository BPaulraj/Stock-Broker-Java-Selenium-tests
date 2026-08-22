package org.bharathi.config;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ConfigReader {

    private static final String DEFAULT_CONFIG_RESOURCE = "environments.json";

    private static final String ENVIRONMENT = System.getProperty("env", "DEV");
    private static final JSONObject ENVIRONMENT_CONFIG = loadEnvironmentConfig();

    private ConfigReader() {
    }

    private static JSONObject loadEnvironmentConfig() {
        String configFile = System.getProperty("config.file");
        try (InputStream in = configFile != null
                ? new FileInputStream(configFile)
                : ConfigReader.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Could not find " + DEFAULT_CONFIG_RESOURCE + " on the classpath");
            }
            JSONObject allEnvironments = new JSONObject(new JSONTokener(in));
            if (!allEnvironments.has(ENVIRONMENT)) {
                throw new IllegalStateException(
                        "No \"" + ENVIRONMENT + "\" entry in " + DEFAULT_CONFIG_RESOURCE + ". Known environments: " + allEnvironments.keySet());
            }
            return allEnvironments.getJSONObject(ENVIRONMENT);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load environment config", e);
        }
    }

    public static String getBaseUrl() {
        return System.getProperty("base.url", ENVIRONMENT_CONFIG.getString("baseUrl"));
    }

    public static String getApiBaseUrl() {
        return System.getProperty("api.base.url", ENVIRONMENT_CONFIG.getString("apiBaseUrl"));
    }

    public static String getTestEmail() {
        return System.getProperty("test.email", ENVIRONMENT_CONFIG.getString("testEmail"));
    }

    public static String getTestPassword() {
        return System.getProperty("test.password", ENVIRONMENT_CONFIG.getString("testPassword"));
    }
}
