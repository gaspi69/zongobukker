package gaspar.web;

import java.net.URL;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

@Slf4j
public class IeWebInstance extends BrowserInstance {

    public IeWebInstance(final URL seleniumServerUrl) throws Exception {
        super(seleniumServerUrl);
    }

    @Override
    protected WebDriver initWebDriver(final URL seleniumServerUrl) {
        log.debug("IE browser set");

        return new RemoteWebDriver(seleniumServerUrl, DesiredCapabilities.internetExplorer());
    }

}
