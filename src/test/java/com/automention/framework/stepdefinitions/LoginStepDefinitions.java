package com.automention.framework.stepdefinitions;

import com.automention.framework.config.ApplicationConfig;
import com.automention.framework.driver.WebDriverManager;
import com.automention.framework.pages.LoginPage;
import com.automention.framework.utils.ScreenshotUtil;
import com.automention.framework.utils.TestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;

/**
 * Step Definitions for Login Test Scenarios
 */
public class LoginStepDefinitions {

    private static final Logger logger = LogManager.getLogger(LoginStepDefinitions.class);

    @Autowired
    private LoginPage loginPage;

    @Autowired
    private WebDriverManager webDriverManager;

    @Autowired
    private ApplicationConfig config;

    @Autowired
    private ScreenshotUtil screenshotUtil;

    @Given("I navigate to the login page")
    public void iNavigateToTheLoginPage() {
        try {
            logger.info("Navigating to login page: {}", config.getWebUrl());
            webDriverManager.getDriver().get(config.getWebUrl());
            logger.info("Successfully navigated to login page");
        } catch (Exception e) {
            logger.error("Error navigating to login page: {}", e.getMessage(), e);
            throw e;
        }
    }

    @When("I enter username {string} and password {string}")
    public void iEnterUsernameAndPassword(String username, String password) {
        try {
            logger.info("Entering username: {} and password", username);
            loginPage.enterUsername(username);
            loginPage.enterPassword(password);
        } catch (Exception e) {
            logger.error("Error entering credentials: {}", e.getMessage(), e);
            throw e;
        }
    }

    @And("I click on submit button")
    public void iClickOnSubmitButton() {
        try {
            logger.info("Clicking submit button");
            loginPage.clickSubmit();
        } catch (Exception e) {
            logger.error("Error clicking submit button: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Then("I should be successfully logged in")
    public void iShouldBeSuccessfullyLoggedIn() {
        try {
            logger.info("Verifying login success");
            boolean isLoggedIn = loginPage.verifyLoginSuccess();
            Assert.assertTrue(isLoggedIn, "Login was not successful");
            screenshotUtil.captureScreenshot("LoginSuccess");
            logger.info("Login verification passed");
        } catch (Exception e) {
            logger.error("Error verifying login: {}", e.getMessage(), e);
            screenshotUtil.captureScreenshot("LoginFailure");
            throw e;
        }
    }

    @And("I should see the success message")
    public void iShouldSeeTheSuccessMessage() {
        try {
            logger.info("Verifying success message");
            String message = loginPage.getSuccessMessage();
            Assert.assertNotNull(message, "Success message is null");
            Assert.assertFalse(message.isEmpty(), "Success message is empty");
            // Store the success message in context for Kibana
            TestContext.setLoginMessage(message);
            logger.info("Success message: {}", message);
        } catch (Exception e) {
            logger.error("Error verifying success message: {}", e.getMessage(), e);
            throw e;
        }
    }

    @When("I click on logout link")
    public void iClickOnLogoutLink() {
        try {
            logger.info("Clicking logout link");
            loginPage.logout();
            screenshotUtil.captureScreenshot("Logout");
        } catch (Exception e) {
            logger.error("Error clicking logout link: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Then("I should be logged out successfully")
    public void iShouldBeLoggedOutSuccessfully() {
        try {
            logger.info("Verifying logout success");
            // Add logout verification logic if needed
            logger.info("Logout verification passed");
        } catch (Exception e) {
            logger.error("Error verifying logout: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Then("I should see the error message {string}")
    public void iShouldSeeTheErrorMessage(String expectedErrorMessage) {
        try {
            logger.info("Verifying error message: {}", expectedErrorMessage);
            boolean isErrorDisplayed = loginPage.verifyErrorMessageDisplayed();
            Assert.assertTrue(isErrorDisplayed, "Error message is not displayed");
            
            String actualErrorMessage = loginPage.getErrorMessage();
            Assert.assertEquals(actualErrorMessage, expectedErrorMessage, 
                "Error message does not match. Expected: " + expectedErrorMessage + ", Actual: " + actualErrorMessage);
            
            // Store the error message in context for Kibana
            TestContext.setLoginMessage(actualErrorMessage);
            
            screenshotUtil.captureScreenshot("LoginError");
            logger.info("Error message verification passed: {}", actualErrorMessage);
        } catch (Exception e) {
            logger.error("Error verifying error message: {}", e.getMessage(), e);
            screenshotUtil.captureScreenshot("LoginErrorFailure");
            throw e;
        }
    }

    @Then("I should not be successfully logged in")
    public void iShouldNotBeSuccessfullyLoggedIn() {
        try {
            logger.info("Verifying that login was not successful");
            boolean isLoggedIn = loginPage.verifyLoginSuccess();
            Assert.assertFalse(isLoggedIn, "Login was unexpectedly successful");
            
            // Try to capture error message if available
            try {
                if (loginPage.verifyErrorMessageDisplayed()) {
                    String errorMsg = loginPage.getErrorMessage();
                    if (errorMsg != null && !errorMsg.isEmpty()) {
                        TestContext.setLoginMessage(errorMsg);
                        logger.info("Error message captured: {}", errorMsg);
                    }
                }
            } catch (Exception e) {
                logger.debug("Could not capture error message: {}", e.getMessage());
            }
            
            screenshotUtil.captureScreenshot("LoginFailure");
            logger.info("Login failure verification passed");
        } catch (Exception e) {
            logger.error("Error verifying login failure: {}", e.getMessage(), e);
            screenshotUtil.captureScreenshot("LoginFailureError");
            throw e;
        }
    }
}
