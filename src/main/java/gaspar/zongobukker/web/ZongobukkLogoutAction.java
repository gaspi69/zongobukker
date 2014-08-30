package gaspar.zongobukker.web;

import gaspar.web.WebAction;
import gaspar.zongobukker.ZongobukkConfiguration;
import gaspar.zongobukker.bean.ZongobukkException;

import org.openqa.selenium.By;

public class ZongobukkLogoutAction extends WebAction {

    private String logoutPage;

    public ZongobukkLogoutAction(final ZongobukkConfiguration configuration) {
        super(configuration.getDriver());
    }

    @Override
    public void run() {
        this.driver.get(this.logoutPage);

        waitForPageLoaded();

        if (!isElementPresent(By.id("UserName")) || !isElementPresent(By.id("Password"))) {
            throw new ZongobukkException("not logged out");
        }
    }

    public void setLogoutPage(final String logoutPage) {
        this.logoutPage = logoutPage;
    }
}
