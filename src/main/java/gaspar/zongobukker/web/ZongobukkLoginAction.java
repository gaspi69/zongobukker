package gaspar.zongobukker.web;

import gaspar.web.WebAction;
import gaspar.zongobukker.ZongobukkSession;
import gaspar.zongobukker.bean.ZongobukkException;

import org.openqa.selenium.By;

public class ZongobukkLoginAction extends WebAction {

    private final ZongobukkSession session;

    private String loginPage;

    public ZongobukkLoginAction(final ZongobukkSession session) {
        super(session.getDriver());
        this.session = session;
    }

    @Override
    protected void innerRun() {
        openPage(this.loginPage);

        this.driver.findElement(By.id("UserName")).sendKeys(this.session.getUserConfiguration().getUsername());
        this.driver.findElement(By.id("Password")).sendKeys(this.session.getUserConfiguration().getPassword());
        this.driver.findElement(By.xpath("//*[@id='main']/table/tbody/tr/td[2]/form/div/fieldset/p/input")).click();

        if (!isElementPresent(By.xpath("//*[@id='user']/a"))) {
            throw new ZongobukkException("not logged in");
        }
    }

    public void setLoginPage(final String loginPage) {
        this.loginPage = loginPage;
    }
}
