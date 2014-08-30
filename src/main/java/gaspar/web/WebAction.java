package gaspar.web;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class WebAction implements Runnable {

    protected final WebDriver driver;

    public WebAction(final WebDriver driver) {
        super();
        this.driver = driver;
    }

    protected void waitForPageLoaded() {
        // final ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
        // @Override
        // public Boolean apply(final WebDriver driver) {
        // return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
        // }
        // };
        //
        // new WebDriverWait(this.driver, 30).until(expectation);

        this.driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);

        if (this.driver.getTitle().contains("404")) {
            throw new IllegalStateException("page not available");
        }

    }

    protected void waitForElementToLoad(final WebElement element) {
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.visibilityOf(element));
    }

    protected boolean isElementPresent(final By by) {
        // return this.driver.findElements(by).size() < 0;
        try {
            this.driver.findElement(by);
            return true;
        } catch (final NoSuchElementException e) {
            return false;
        }
    }

}
