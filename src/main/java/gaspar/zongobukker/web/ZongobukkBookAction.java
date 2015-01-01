package gaspar.zongobukker.web;

import gaspar.web.UrlBuilder;
import gaspar.web.WebAction;
import gaspar.zongobukker.ZongobukkSession;
import gaspar.zongobukker.bean.Timeslot;
import gaspar.zongobukker.bean.Timeslot.Status;
import gaspar.zongobukker.bean.ZongobukkContext;
import gaspar.zongobukker.util.DateUtil;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

@Slf4j
public class ZongobukkBookAction extends WebAction {

    private final ZongobukkContext zongobukkContext;

    private final ExecutorService bookExecutorService = Executors.newCachedThreadPool();

    private UrlBuilder urlBuilder;

    private String subscribeUrlPattern;

    private String succesfullMessage;
    private String alreadyBookedMessage;

    private ZongobukkBookAction(final ZongobukkSession session) {
        super(session.getDriver());
        this.zongobukkContext = session.getZongobukkContext();
    }

    @Override
    protected void innerRun() {
        try {
            for (final Timeslot timeslot : this.zongobukkContext.getRequiredTimeslots()) {
                if (timeslot.getStatus().equals(Timeslot.Status.TO_BE_BOOKED)) {
                    // this.bookExecutorService.execute(new BookTask(timeslot));
                    new BookTask(timeslot).run();
                }
            }

            this.bookExecutorService.shutdown();
            this.bookExecutorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (final InterruptedException e) {
            log.error("", e);
        }
    }

    private class BookTask implements Runnable {

        private final Timeslot timeslot;

        public BookTask(final Timeslot timeslot) {
            super();
            this.timeslot = timeslot;
        }

        @Override
        public void run() {
            log.info("Booking slot: {}", this.timeslot);

            try {
                final String actionLink = ZongobukkBookAction.this.urlBuilder.buildUrl(ZongobukkBookAction.this.subscribeUrlPattern,
                        this.timeslot.getRoomNumber(), DateUtil.getHours(this.timeslot.getStartDate())).toExternalForm();

                log.debug("Booking {} on URL {}", this.timeslot, actionLink);

                openPage(actionLink);

                handleAlertBox();

                this.timeslot.setStatus(Status.MYBOOKING);
                this.timeslot.getComment().append(" - Booking OK");
            } catch (final MalformedURLException e) {
                log.error("wrong URL", e);
            }
        }

        private void handleAlertBox() {
            if (!(ZongobukkBookAction.this.driver instanceof HtmlUnitDriver)) {
                try {
                    final Alert alertBox = ZongobukkBookAction.this.driver.switchTo().alert();

                    if (alertBox != null) {
                        final String alertText = alertBox.getText();

                        log.debug("Alert response: {}", alertText);

                        if (alertText == null) {
                            throw new IllegalStateException("Alert text is null");
                        } else if (alertText.matches(ZongobukkBookAction.this.alreadyBookedMessage)) {
                            log.warn("Timeslot already booked: {}", this.timeslot);
                        } else if (alertText.matches(ZongobukkBookAction.this.succesfullMessage)) {
                            log.info("Timeslot successfully booked: {}", this.timeslot);
                        }

                        alertBox.accept();
                    } else {
                        throw new IllegalStateException("Alertbox not shown");
                    }
                } catch (final WebDriverException e) {
                    if (e.getCause() instanceof UnsupportedOperationException) {
                        log.warn("alert link not supported: {}", e.getMessage());
                    } else {
                        throw e;
                    }
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

    public void setSubscribeUrlPattern(final String subscribeUrlPattern) {
        this.subscribeUrlPattern = subscribeUrlPattern;
    }

    public void setUrlBuilder(final UrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }

}
