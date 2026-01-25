package com.automention.framework.hooks;

import com.automention.framework.config.ApplicationConfig;
import com.automention.framework.driver.WebDriverManager;
import com.automention.framework.utils.ElasticSearchUtil;
import com.automention.framework.utils.ScreenshotUtil;
import com.automention.framework.utils.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * Cucumber Hooks for test setup and teardown
 */
public class Hooks {

    private static final Logger logger = LogManager.getLogger(Hooks.class);

    @Autowired
    private WebDriverManager webDriverManager;

    @Autowired
    private ElasticSearchUtil elasticSearchUtil;

    @Autowired
    private ScreenshotUtil screenshotUtil;

    @Autowired
    private ApplicationConfig config;

    @Before
    public void setUp(Scenario scenario) {
        logger.info("Starting scenario: {}", scenario.getName());
        elasticSearchUtil.initializeClient();
    }

    @After
    public void tearDown(Scenario scenario) {
        try {
            String status = scenario.isFailed() ? "FAILED" : "PASSED";
            logger.info("Scenario '{}' finished with status: {}", scenario.getName(), status);

            // Capture final screenshot (best-effort; will be null if no WebDriver)
            String screenshotPath = null;
            try {
                String safeName = toSafeFileName(scenario.getName());
                screenshotPath = screenshotUtil.captureScreenshot(safeName + "_" + status);
            } catch (Exception e) {
                logger.warn("Could not capture screenshot in @After: {}", e.getMessage());
            }

            // Extract feature name from scenario URI
            String featureName = extractFeatureName(scenario.getUri().toString());

            // Send result to Elasticsearch for Kibana dashboards
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("tags", scenario.getSourceTagNames());
            additionalData.put("thread", Thread.currentThread().getName());
            additionalData.put("browser", config.getBrowser());
            additionalData.put("gridUrl", config.getSeleniumGridUrl());
            additionalData.put("featurename", featureName);
            additionalData.put("testCaseName", featureName);
            
            // Get login message from context (success or error message)
            String loginMessage = TestContext.getLoginMessage();
            if (loginMessage != null && !loginMessage.isEmpty()) {
                additionalData.put("loginMessage", loginMessage);
            }

            String errorMessage = scenario.isFailed() ? "Scenario failed" : null;
            elasticSearchUtil.sendTestResult(scenario.getName(), status, screenshotPath, errorMessage, additionalData);
            
            // Clear the context after sending to Elasticsearch
            TestContext.clearLoginMessage();
            
            // Quit WebDriver
            if (webDriverManager != null) {
                webDriverManager.quitDriver();
            }
            
            // Close Elasticsearch client
            if (elasticSearchUtil != null) {
                elasticSearchUtil.closeClient();
            }
        } catch (Exception e) {
            logger.error("Error in tearDown: {}", e.getMessage(), e);
        }
    }

    private static String toSafeFileName(String input) {
        if (input == null || input.isBlank()) {
            return "scenario";
        }
        return input
                .trim()
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_");
    }

    /**
     * Extract feature file name from scenario URI
     */
    private static String extractFeatureName(String uri) {
        if (uri == null || uri.isEmpty()) {
            return "Unknown";
        }
        try {
            // Extract filename from URI (e.g., "file:///path/to/Scenario1_LoginTest.feature" -> "Scenario1_LoginTest.feature")
            String fileName = uri.substring(uri.lastIndexOf('/') + 1);
            if (fileName.contains("\\")) {
                fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);
            }
            // Remove .feature extension for cleaner display
            if (fileName.endsWith(".feature")) {
                fileName = fileName.substring(0, fileName.length() - 8);
            }
            return fileName;
        } catch (Exception e) {
            logger.warn("Error extracting feature name from URI: {}", uri);
            return "Unknown";
        }
    }
}
