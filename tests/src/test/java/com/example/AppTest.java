package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest {

    private WebDriver driver;
    private String baseUrl;
    private static String uniqueUsername;
    private static final String PASSWORD = "password123";

    @BeforeAll
    public static void setupClass() {
        uniqueUsername = "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        
        baseUrl = System.getenv("APP_URL");
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "http://localhost:5000";
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testLoginPageTitle() {
        driver.get(baseUrl + "/login");
        assertEquals("Login", driver.getTitle());
    }

    @Test
    @Order(2)
    public void testRegisterPageTitle() {
        driver.get(baseUrl + "/register");
        assertEquals("Register", driver.getTitle());
    }

    @Test
    @Order(3)
    public void testProtectedIndexRoute() {
        driver.get(baseUrl + "/");
        // Should redirect to login
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test
    @Order(4)
    public void testNavigationBetweenLoginAndRegister() {
        driver.get(baseUrl + "/login");
        driver.findElement(By.id("register-link")).click();
        assertTrue(driver.getCurrentUrl().contains("/register"));
        
        driver.findElement(By.id("login-link")).click();
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test
    @Order(5)
    public void testLoginWithEmptyFields() {
        driver.get(baseUrl + "/login");
        driver.findElement(By.id("login-button")).click();
        WebElement error = driver.findElement(By.id("error-message"));
        assertEquals("Please fill all fields", error.getText());
    }

    @Test
    @Order(6)
    public void testRegisterWithEmptyFields() {
        driver.get(baseUrl + "/register");
        driver.findElement(By.id("register-button")).click();
        WebElement error = driver.findElement(By.id("error-message"));
        assertEquals("Please fill all fields", error.getText());
    }

    @Test
    @Order(7)
    public void testRegisterNewUser() {
        driver.get(baseUrl + "/register");
        driver.findElement(By.id("username")).sendKeys(uniqueUsername);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("register-button")).click();
        
        // Should redirect to login upon success
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test
    @Order(8)
    public void testRegisterExistingUser() {
        driver.get(baseUrl + "/register");
        driver.findElement(By.id("username")).sendKeys(uniqueUsername);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("register-button")).click();
        
        WebElement error = driver.findElement(By.id("error-message"));
        assertEquals("Username already exists", error.getText());
    }

    @Test
    @Order(9)
    public void testLoginWithInvalidCredentials() {
        driver.get(baseUrl + "/login");
        driver.findElement(By.id("username")).sendKeys(uniqueUsername);
        driver.findElement(By.id("password")).sendKeys("wrongpassword");
        driver.findElement(By.id("login-button")).click();
        
        WebElement error = driver.findElement(By.id("error-message"));
        assertEquals("Invalid credentials", error.getText());
    }

    @Test
    @Order(10)
    public void testSuccessfulLogin() {
        login();
        assertTrue(driver.getCurrentUrl().endsWith("/"));
        WebElement welcome = driver.findElement(By.id("welcome-message"));
        assertTrue(welcome.getText().contains(uniqueUsername));
    }

    @Test
    @Order(11)
    public void testAddTodoItem() {
        login();
        driver.findElement(By.id("new-task")).sendKeys("Buy milk");
        driver.findElement(By.id("add-button")).click();
        
        List<WebElement> todos = driver.findElements(By.className("todo-item"));
        assertTrue(todos.size() > 0);
        assertTrue(todos.get(todos.size()-1).getText().contains("Buy milk"));
    }

    @Test
    @Order(12)
    public void testAddEmptyTodoItem() {
        login();
        int initialCount = driver.findElements(By.className("todo-item")).size();
        
        driver.findElement(By.id("add-button")).click();
        
        int finalCount = driver.findElements(By.className("todo-item")).size();
        assertEquals(initialCount, finalCount);
    }

    @Test
    @Order(13)
    public void testMultipleTodos() {
        login();
        int initialCount = driver.findElements(By.className("todo-item")).size();
        
        driver.findElement(By.id("new-task")).sendKeys("Task 1");
        driver.findElement(By.id("add-button")).click();
        driver.findElement(By.id("new-task")).sendKeys("Task 2");
        driver.findElement(By.id("add-button")).click();
        
        int finalCount = driver.findElements(By.className("todo-item")).size();
        assertEquals(initialCount + 2, finalCount);
    }

    @Test
    @Order(14)
    public void testDeleteTodoItem() {
        login();
        driver.findElement(By.id("new-task")).sendKeys("Task to delete");
        driver.findElement(By.id("add-button")).click();
        
        List<WebElement> todos = driver.findElements(By.className("todo-item"));
        int initialCount = todos.size();
        
        // Find the delete button of the last added task
        WebElement deleteBtn = todos.get(initialCount - 1).findElement(By.className("delete-button"));
        deleteBtn.click();
        
        int finalCount = driver.findElements(By.className("todo-item")).size();
        assertEquals(initialCount - 1, finalCount);
    }

    @Test
    @Order(15)
    public void testLogout() {
        login();
        driver.findElement(By.id("logout-button")).click();
        assertTrue(driver.getCurrentUrl().contains("/login"));
        
        // verify cannot access index
        driver.get(baseUrl + "/");
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    private void login() {
        driver.get(baseUrl + "/login");
        driver.findElement(By.id("username")).sendKeys(uniqueUsername);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-button")).click();
    }
}
