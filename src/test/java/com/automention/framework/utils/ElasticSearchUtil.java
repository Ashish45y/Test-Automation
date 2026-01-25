package com.automention.framework.utils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.automention.framework.config.ApplicationConfig;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Elasticsearch Utility for Reporting
 * Sends test results to Elasticsearch for Kibana visualization
 */
@Component
public class ElasticSearchUtil {

    private static final Logger logger = LogManager.getLogger(ElasticSearchUtil.class);

    @Autowired
    private ApplicationConfig config;

    private final ThreadLocal<RestClient> restClientThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<ElasticsearchTransport> transportThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<ElasticsearchClient> clientThreadLocal = new ThreadLocal<>();

    /**
     * Initialize Elasticsearch client
     */
    public void initializeClient() {
        if (!config.isElasticsearchEnabled()) {
            logger.info("Elasticsearch is disabled in configuration");
            return;
        }

        try {
            RestClient restClient = RestClient.builder(
                    new HttpHost(config.getElasticsearchHost(), config.getElasticsearchPort(), "http")
            ).build();

            ElasticsearchTransport transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper());

            ElasticsearchClient client = new ElasticsearchClient(transport);
            restClientThreadLocal.set(restClient);
            transportThreadLocal.set(transport);
            clientThreadLocal.set(client);

            logger.info("Elasticsearch client initialized successfully (thread={})", Thread.currentThread().getName());
        } catch (Exception e) {
            logger.error("Error initializing Elasticsearch client: {}", e.getMessage(), e);
        }
    }

    /**
     * Send test result to Elasticsearch
     */
    public void sendTestResult(String scenarioName, String status, String screenshotPath, 
                               String errorMessage, Map<String, Object> additionalData) {
        ElasticsearchClient client = clientThreadLocal.get();
        if (!config.isElasticsearchEnabled() || client == null) {
            logger.debug("Elasticsearch is disabled or client not initialized");
            return;
        }

        try {
            Map<String, Object> document = new HashMap<>();
            document.put("scenario", scenarioName);
            document.put("status", status);
            document.put("timestamp", LocalDateTime.now().toString());
            document.put("screenshotPath", screenshotPath);
            document.put("errorMessage", errorMessage != null ? errorMessage : "");
            
            // Extract featurename, testCaseName, and loginMessage from additionalData
            String featureName = "Unknown";
            String testCaseName = scenarioName; // Default to scenario name if not found
            String loginMessage = ""; // Login success or error message
            if (additionalData != null) {
                Object featureNameObj = additionalData.get("featurename");
                if (featureNameObj != null) {
                    featureName = featureNameObj.toString();
                }
                Object testCaseNameObj = additionalData.get("testCaseName");
                if (testCaseNameObj != null) {
                    testCaseName = testCaseNameObj.toString();
                }
                Object loginMessageObj = additionalData.get("loginMessage");
                if (loginMessageObj != null) {
                    loginMessage = loginMessageObj.toString();
                }
                document.putAll(additionalData);
            }
            // Add featurename and testCaseName as top-level fields (like status) for Kibana keyword breakdown
            document.put("featurename", featureName);
            document.put("testCaseName", testCaseName);
            
            // Add loginMessage as top-level field for Kibana visualization
            // This will show "Congratulations student. You successfully logged in!" or "Your password is invalid!"
            document.put("loginMessage", loginMessage);
            
            // Add combined field for breakdown showing feature name with status
            // This allows Kibana to show "FeatureName - PASSED" or "FeatureName - FAILED" in breakdowns
            document.put("featurename_status", featureName + " - " + status);

            // Encode screenshot as base64 if path is provided
            if (screenshotPath != null && !screenshotPath.isEmpty()) {
                try {
                    File screenshotFile = new File(screenshotPath);
                    if (screenshotFile.exists()) {
                        byte[] fileContent = Files.readAllBytes(screenshotFile.toPath());
                        String base64Image = java.util.Base64.getEncoder().encodeToString(fileContent);
                        document.put("screenshotBase64", base64Image);
                    }
                } catch (Exception e) {
                    logger.warn("Error encoding screenshot: {}", e.getMessage());
                }
            }

            IndexRequest<Map<String, Object>> request = IndexRequest.of(i -> i
                    .index(config.getElasticsearchIndex())
                    .document(document)
            );

            client.index(request);
            logger.info("Test result sent to Elasticsearch: {} - {}", scenarioName, status);

        } catch (Exception e) {
            logger.error("Error sending test result to Elasticsearch: {}", e.getMessage(), e);
        }
    }

    /**
     * Close Elasticsearch client
     */
    public void closeClient() {
        try {
            ElasticsearchTransport transport = transportThreadLocal.get();
            RestClient restClient = restClientThreadLocal.get();

            if (transport != null) {
                try {
                    transport.close();
                } catch (Exception e) {
                    logger.warn("Error closing Elasticsearch transport: {}", e.getMessage());
                }
            }
            if (restClient != null) {
                try {
                    restClient.close();
                } catch (Exception e) {
                    logger.warn("Error closing Elasticsearch RestClient: {}", e.getMessage());
                }
            }

            clientThreadLocal.remove();
            transportThreadLocal.remove();
            restClientThreadLocal.remove();

            logger.info("Closed Elasticsearch client (thread={})", Thread.currentThread().getName());
        } catch (Exception e) {
            logger.error("Error closing Elasticsearch client: {}", e.getMessage(), e);
        }
    }
}
