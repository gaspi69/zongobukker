package gaspar.zongobukker.web;

import gaspar.web.WebAction;
import gaspar.zongobukker.ZongobukkSession;
import gaspar.zongobukker.bean.ZongobukkException;

import org.openqa.selenium.By;

public class ZongobukkLogoutAction extends WebAction {

    private String logoutPage;

    public ZongobukkLogoutAction(final ZongobukkSession session) {
        super(session.getDriver());
    }

    @Override
    protected void innerRun() {
        openPage(this.logoutPage);

        if (!isElementPresent(By.id("UserName")) || !isElementPresent(By.id("Password"))) {
            throw new ZongobukkException("not logged out");
        }
    }

    public void setLogoutPage(final String logoutPage) {
        this.logoutPage = logoutPage;
    }
}
