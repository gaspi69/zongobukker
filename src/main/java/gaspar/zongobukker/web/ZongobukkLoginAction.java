package gaspar.zongobukker.web;

import gaspar.web.WebAction;
import gaspar.zongobukker.ZongobukkConfiguration;
import gaspar.zongobukker.bean.ZongobukkException;
import gaspar.zongobukker.user.ZongobukkUserContext;

import org.openqa.selenium.By;

public class ZongobukkLoginAction extends WebAction {

    private final ZongobukkUserContext zongobukkUserContext;

    private String loginPage;

    public ZongobukkLoginAction(final ZongobukkConfiguration configuration) {
        super(configuration.getDriver());
        this.zongobukkUserContext = configuration.getZongobukkUserContext();
    }

    @Override
    public void run() {
        this.driver.get(this.loginPage);

        waitForPageLoaded();

        this.driver.findElement(By.id("UserName")).sendKeys(this.zongobukkUserContext.getUsername());
        this.driver.findElement(By.id("Password")).sendKeys(this.zongobukkUserContext.getPassword());
        this.driver.findElement(By.xpath("//*[@id='main']/table/tbody/tr/td[2]/form/div/fieldset/p/input")).click();

        waitForPageLoaded();

        if (!isElementPresent(By.xpath("//*[@id='user']/a"))) {
            throw new ZongobukkException("not logged in");
        }
    }

    public void setLoginPage(final String loginPage) {
        this.loginPage = loginPage;
    }
}
