package com.automention.framework.utils;

/**
 * Test Context to share data between step definitions and hooks
 * Uses ThreadLocal to ensure thread safety in parallel execution
 */
public class TestContext {
    
    private static final ThreadLocal<String> loginMessage = new ThreadLocal<>();
    
    /**
     * Set the login message (success or error message)
     */
    public static void setLoginMessage(String message) {
        loginMessage.set(message);
    }
    
    /**
     * Get the login message
     */
    public static String getLoginMessage() {
        return loginMessage.get();
    }
    
    /**
     * Clear the login message (should be called in @After hook)
     */
    public static void clearLoginMessage() {
        loginMessage.remove();
    }
}
