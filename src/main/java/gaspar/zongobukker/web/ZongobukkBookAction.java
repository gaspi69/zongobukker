package gaspar.zongobukker.web;

import gaspar.web.UrlBuilder;
import gaspar.web.WebAction;
import gaspar.zongobukker.ZongobukkConfiguration;
import gaspar.zongobukker.bean.Timeslot;
import gaspar.zongobukker.user.ZongobukkUserContext;
import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.Alert;

@Slf4j
public class ZongobukkBookAction extends WebAction {

    private final ZongobukkUserContext zongobukkUserContext;

    private UrlBuilder urlBuilder;

    private String subscribeUrlPattern;
    private String unsubscribeUrlPattern;

    private String succesfullMessage;
    private String alreadyBookedMessage;

    private ZongobukkBookAction(final ZongobukkConfiguration configuration) {
        super(configuration.getDriver());
        this.zongobukkUserContext = configuration.getZongobukkUserContext();
    }

    @Override
    public void run() {

        for (final Timeslot timeslot : this.zongobukkUserContext.getRequiredTimeslots()) {
            if (timeslot.getStatus().equals(Timeslot.Status.MYBOOKING)) {
                log.info("Booking slot: {}", timeslot);

                final String actionLink = null; // this.urlBuilder.buildUrl(this.subscribeUrlPattern,
                                                // timeslot.getStartDate());

                log.debug("Booking {} on URL {}", timeslot, actionLink);

                this.driver.get(actionLink);

                final Alert alertBox = this.driver.switchTo().alert();

                if (alertBox != null) {
                    final String alertText = alertBox.getText();

                    log.debug("Alert response: {}", alertText);

                    if (alertText == null) {
                        throw new IllegalStateException("Alert text is null");
                    } else if (alertText.matches(this.alreadyBookedMessage)) {
                        log.warn("Timeslot already booked: {}", timeslot);
                    } else if (alertText.matches(this.succesfullMessage)) {
                        log.info("Timeslot successfully booked: {}", timeslot);
                    }

                    alertBox.accept();
                } else {
                    throw new IllegalStateException("Alertbox not shown");
                }
            }
        }
    }

    public void setSuccesfullMessage(final String succesfullMessage) {
        this.succesfullMessage = succesfullMessage;
    }

    public void setAlreadyBookedMessage(final String alreadyBookedMessage) {
        this.alreadyBookedMessage = alreadyBookedMessage;
    }

}
