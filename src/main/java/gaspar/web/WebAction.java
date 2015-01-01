package gaspar.web;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Slf4j
public abstract class WebAction implements Runnable {

    protected final WebDriver driver;

    public WebAction(final WebDriver driver) {
        super();
        this.driver = driver;
    }

    protected void openPage(final String link) {
        this.driver.get(link);
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

    @Override
    public void run() {
        try {
            innerRun();
        } catch (final WebDriverException e) {
            log.error("unhandled web error", e);
        }
    }

    protected abstract void innerRun();

}
