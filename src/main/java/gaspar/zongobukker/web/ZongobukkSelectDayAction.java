package gaspar.zongobukker.web;

import gaspar.web.UrlBuilder;
import gaspar.web.WebAction;
import gaspar.zongobukker.bean.ZongobukkException;

import java.net.MalformedURLException;
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

    private UrlBuilder urlBuilder;

    private String daypickerUrlPatternLink;

    public ZongobukkSelectDayAction(final WebDriver driver, final Calendar searchDay) {
        super(driver);
        this.searchDay = searchDay;
    }

    @Override
    protected void innerRun() {
        try {
            final String actualDay = DAY_FORMAT.format(this.searchDay.getTime());

            log.info("Selecting day: {}", actualDay);

            openPage(this.urlBuilder.buildUrl(this.daypickerUrlPatternLink, actualDay).toExternalForm());

            final WebElement dayWebElement = this.driver.findElement(By.xpath("//*[@id='main']/table/tbody/tr/td[1]/div[4]/font"));

            if (dayWebElement == null || !actualDay.equals(dayWebElement.getText())) {
                throw new ZongobukkException("Not able to book for day: " + actualDay);
            }
        } catch (final MalformedURLException e) {
            log.error("invalid URL", e);
        }
    }

    public void setDaypickerLink(final String daypickerLink) {
        this.daypickerUrlPatternLink = daypickerLink;
    }

    public void setUrlBuilder(final UrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }

}
