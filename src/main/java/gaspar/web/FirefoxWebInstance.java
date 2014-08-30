package gaspar.web;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.DisposableBean;

@Slf4j
public class FirefoxWebInstance implements DisposableBean {

    // private final SeleniumServer server;

    protected final WebDriver driver;

    public FirefoxWebInstance(final URL seleniumServerUrl) throws Exception {
        // this.server = new SeleniumServer();
        // this.server.start();

        log.info("Initializing firefox ...");

        final DesiredCapabilities ffCapability = DesiredCapabilities.firefox();

        final FirefoxProfile firefoxprofile = new FirefoxProfile();
        firefoxprofile.setAssumeUntrustedCertificateIssuer(true);
        ffCapability.setCapability(FirefoxDriver.PROFILE, firefoxprofile);

        this.driver = new RemoteWebDriver(seleniumServerUrl, ffCapability);

        this.driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
    }

    @Override
    public void destroy() throws Exception {
        log.info("Closing firefox ...");

        this.driver.quit();
    }

    public WebDriver getDriver() {
        return this.driver;
    }

}
