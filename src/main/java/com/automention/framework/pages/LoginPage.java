package com.automention.framework.pages;

import com.automention.framework.driver.WebDriverManager;
import io.cucumber.spring.ScenarioScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Login Page Object Model
 * Handles login page interactions
 */
@Component
@ScenarioScope
public class LoginPage {

    private static final Logger logger = LogManager.getLogger(LoginPage.class);

    @Autowired
    private WebDriverManager webDriverManager;

    @FindBy(id = "username")
    private WebElement usernameField;

    @FindBy(id = "password")
    private WebElement passwordField;

    @FindBy(id = "submit")
    private WebElement submitButton;

    @FindBy(className = "post-title")
    private WebElement successMessage;

    @FindBy(linkText = "Log out")
    private WebElement logoutLink;

    // Error message can be found by id, class, or xpath - trying multiple approaches
    @FindBy(id = "error")
    private WebElement errorMessageById;

    @FindBy(className = "error")
    private WebElement errorMessageByClass;

    @FindBy(xpath = "//div[contains(@class, 'error') or contains(@id, 'error')]")
    private WebElement errorMessageByXpath;

    private WebDriver driver;
    private WebDriverWait wait;

    /**
     * Initialize page elements
     */
    public void initElements() {
        driver = webDriverManager.getDriver();
        PageFactory.initElements(driver, this);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    /**
     * Enter username
     */
    public void enterUsername(String username) {
        try {
            if (wait == null || driver == null) {
                initElements();
            }
            wait.until(ExpectedConditions.visibilityOf(usernameField));
            usernameField.clear();
            usernameField.sendKeys(username);
            logger.info("Username entered: {}", username);
        } catch (Exception e) {
            logger.error("Error entering username: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Enter password
     */
    public void enterPassword(String password) {
        try {
            if (wait == null || driver == null) {
                initElements();
            }
            wait.until(ExpectedConditions.visibilityOf(passwordField));
            passwordField.clear();
            passwordField.sendKeys(password);
            logger.info("Password entered");
        } catch (Exception e) {
            logger.error("Error entering password: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Click submit button
     */
    public void clickSubmit() {
        try {
            if (wait == null || driver == null) {
                initElements();
            }
            wait.until(ExpectedConditions.elementToBeClickable(submitButton));
            submitButton.click();
            logger.info("Submit button clicked");
        } catch (Exception e) {
            logger.error("Error clicking submit button: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Perform login
     */
    public void login(String username, String password) {
        initElements();
        enterUsername(username);
        enterPassword(password);
        clickSubmit();
    }

    /**
     * Verify post-login success message
     */
    public boolean verifyLoginSuccess() {
        try {
            initElements();
            wait.until(ExpectedConditions.visibilityOf(successMessage));
            boolean isDisplayed = successMessage.isDisplayed();
            logger.info("Login success verification: {}", isDisplayed);
            return isDisplayed;
        } catch (Exception e) {
            logger.error("Error verifying login success: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get success message text
     */
    public String getSuccessMessage() {
        try {
            initElements();
            wait.until(ExpectedConditions.visibilityOf(successMessage));
            return successMessage.getText();
        } catch (Exception e) {
            logger.error("Error getting success message: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * Get error message element (tries multiple selectors)
     */
    private WebElement getErrorMessageElement() {
        initElements();
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        
        // Try to find error message using multiple selectors
        try {
            shortWait.until(ExpectedConditions.visibilityOf(errorMessageById));
            return errorMessageById;
        } catch (Exception e) {
            logger.debug("Error message not found by id, trying class name");
        }
        
        try {
            shortWait.until(ExpectedConditions.visibilityOf(errorMessageByClass));
            return errorMessageByClass;
        } catch (Exception e) {
            logger.debug("Error message not found by class, trying xpath");
        }
        
        try {
            shortWait.until(ExpectedConditions.visibilityOf(errorMessageByXpath));
            return errorMessageByXpath;
        } catch (Exception e) {
            logger.debug("Error message not found by xpath, trying text-based search");
        }
        
        // Last resort: try to find by text content
        try {
            org.openqa.selenium.By byText = org.openqa.selenium.By.xpath("//*[contains(text(), 'password is invalid') or contains(text(), 'Password is invalid') or contains(text(), 'invalid')]");
            shortWait.until(ExpectedConditions.presenceOfElementLocated(byText));
            return driver.findElement(byText);
        } catch (Exception e) {
            logger.error("Could not find error message element using any selector");
            throw new org.openqa.selenium.NoSuchElementException("Error message element not found");
        }
    }

    /**
     * Get error message text
     */
    public String getErrorMessage() {
        try {
            WebElement errorElement = getErrorMessageElement();
            String errorText = errorElement.getText();
            logger.info("Error message found: {}", errorText);
            return errorText;
        } catch (Exception e) {
            logger.error("Error getting error message: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * Verify error message is displayed
     */
    public boolean verifyErrorMessageDisplayed() {
        try {
            WebElement errorElement = getErrorMessageElement();
            boolean isDisplayed = errorElement.isDisplayed();
            logger.info("Error message displayed: {}", isDisplayed);
            return isDisplayed;
        } catch (Exception e) {
            logger.error("Error message not found: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Perform logout
     */
    public void logout() {
        try {
            initElements();
            wait.until(ExpectedConditions.elementToBeClickable(logoutLink));
            logoutLink.click();
            logger.info("Logout performed successfully");
        } catch (Exception e) {
            logger.error("Error performing logout: {}", e.getMessage(), e);
            throw e;
        }
    }
}
