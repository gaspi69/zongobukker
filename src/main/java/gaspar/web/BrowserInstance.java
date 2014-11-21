package gaspar.web;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.DisposableBean;

@Slf4j
public abstract class BrowserInstance implements DisposableBean {

    protected final WebDriver driver;

    public BrowserInstance(final URL seleniumServerUrl) throws Exception {
        log.info("Initializing browser instance, connecting to {} server ... ", seleniumServerUrl);

        this.driver = initWebDriver(seleniumServerUrl);

        this.driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
    }

    protected abstract WebDriver initWebDriver(final URL seleniumServerUrl);

    @Override
    public void destroy() throws Exception {
        log.info("Closing browser ...");

        this.driver.quit();
    }

    public WebDriver getDriver() {
        return this.driver;
    }

}
