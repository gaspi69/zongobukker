package gaspar.zongobukker.web;

import gaspar.web.WebAction;
import gaspar.zongobukker.bean.ZongobukkException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public class ZongobukkSelectDayAction extends WebAction {

    private static final DateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final Calendar searchDay;

    private String daypickerLink;

    public ZongobukkSelectDayAction(final WebDriver driver, final Calendar searchDay) {
        super(driver);
        this.searchDay = searchDay;
    }

    @Override
    public void run() {
        final String actualDay = DAY_FORMAT.format(this.searchDay.getTime());

        log.info("Selecting day: {}", actualDay);

        this.driver.get(this.daypickerLink + actualDay);

        final WebElement dayWebElement = this.driver.findElement(By.xpath("//*[@id='main']/table/tbody/tr/td[1]/div[4]/font"));

        if (dayWebElement == null || !actualDay.equals(dayWebElement.getText())) {
            throw new ZongobukkException("Not able to book for day: " + actualDay);
        }

    }

    public void setDaypickerLink(final String daypickerLink) {
        this.daypickerLink = daypickerLink;
    }
}
