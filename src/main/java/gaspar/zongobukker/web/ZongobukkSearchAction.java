package gaspar.zongobukker.web;

import gaspar.web.UrlBuilder;
import gaspar.web.WebAction;
import gaspar.zongobukker.ZongobukkSession;
import gaspar.zongobukker.bean.Timeslot;
import gaspar.zongobukker.bean.Timeslot.Status;
import gaspar.zongobukker.bean.ZongobukkContext;
import gaspar.zongobukker.bean.ZongobukkException;
import gaspar.zongobukker.util.DateUtil;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.google.common.primitives.Ints;

@Slf4j
public class ZongobukkSearchAction extends WebAction {

    private static final DateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final DateFormat TIMESLOT_FORMAT = new SimpleDateFormat("HH:mm");

    private final ZongobukkContext zongobukkContext;

    // private int bookPeriodInDay;

    private String daypickerLink;

    private UrlBuilder urlBuilder;

    private int[] validRoomNumbers;

    private ZongobukkSearchAction(final ZongobukkSession session) {
        super(session.getDriver());
        this.zongobukkContext = session.getZongobukkContext();
    }

    @Override
    protected void innerRun() {
        for (final Calendar searchDay : searchOnDays()) {
            try {
                searchByDay(searchDay);
            } catch (final ZongobukkException e) {
                log.warn("search failed for day {}, detail: {}", DateFormatUtils.ISO_DATETIME_FORMAT.format(searchDay.getTime()), e);
            } catch (final RuntimeException e) {
                log.error("unhandled exception, when searching for " + DateFormatUtils.ISO_DATETIME_FORMAT.format(searchDay.getTime()), e);
            } catch (final MalformedURLException e) {
                log.error("invalid URL", e);
            }
        }
    }

    private void searchByDay(final Calendar searchDay) throws MalformedURLException {
        selectDay(searchDay);

        final Collection<WebElement> zongoRooms = searchRooms();

        if (zongoRooms.isEmpty()) {
            throw new ZongobukkException("Rooms not found");
        }

        for (final WebElement tableRoom : zongoRooms) {
            try {
                searchTimeslotsByDayAndRoom(searchDay, tableRoom);
            } catch (final RuntimeException e) {
                log.error("unhandled exception", e);
            }
        }
    }

    private void selectDay(final Calendar searchDay) throws MalformedURLException {
        final String actualDay = DAY_FORMAT.format(searchDay.getTime());

        log.info("Selecting day: {}", actualDay);

        openPage(this.urlBuilder.buildUrl(this.daypickerLink, actualDay).toExternalForm());

        final WebElement dayWebElement = this.driver.findElement(By.xpath("//*[@id='main']/table/tbody/tr/td[1]/div[4]/font"));

        if (dayWebElement == null || !actualDay.equals(dayWebElement.getText())) {
            throw new ZongobukkException("Not able to book for day: " + actualDay);
        }
    }

    private void searchTimeslotsByDayAndRoom(final Calendar searchDay, final WebElement tableRoomElement) {
        final Integer roomNumber = Integer.valueOf(tableRoomElement.findElement(By.tagName("strong")).getText());

        log.trace("Room found: {}", roomNumber);

        if (!Ints.asList(this.validRoomNumbers).contains(roomNumber.intValue())) {
            log.trace("Parsing of invalid room skipped: {}", roomNumber);
            return;
        } else {
            log.debug("Parsing room {} on {}", roomNumber, DateFormatUtils.ISO_DATETIME_FORMAT.format(searchDay));

            for (final Status status : Arrays.asList(Timeslot.Status.FREE, Timeslot.Status.BOOKED, Timeslot.Status.MYBOOKING,
                    Timeslot.Status.TEMPORARILYBLOCKED)) {
                parseTimeElements(searchDay, tableRoomElement, roomNumber, status);
            }
        }
    }

    private void parseTimeElements(final Calendar searchDay, final WebElement tableRoom, final int roomNumber, final Timeslot.Status status) {
        for (final WebElement tdElement : tableRoom.findElements(By.className(status.toString().toLowerCase()))) {
            WebElement timeslotElement = tdElement;

            if (status.equals(Timeslot.Status.MYBOOKING) || status.equals(Timeslot.Status.FREE)) {
                final WebElement linkElement = tdElement.findElement(By.tagName("a"));
                if (linkElement != null) {
                    timeslotElement = linkElement;
                }
            }

            try {
                final Timeslot timeslot = new Timeslot();

                timeslot.setStartDate(parseTime(searchDay, timeslotElement));
                timeslot.setStatus(status);
                timeslot.setRoomNumber(roomNumber);

                this.zongobukkContext.getCurrentTimeslots().add(timeslot);

                log.trace("Timeslot succesfully added: {}", timeslot);
            } catch (final ParseException e) {
                log.error("Timeslot cannot parsed: {}", timeslotElement, e);
            }
        }
    }

    private Collection<WebElement> searchRooms() {
        final Collection<WebElement> zongoRooms = new HashSet<WebElement>();

        final int cols = 14; // 14
        final int rows = 1; // 2
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                zongoRooms.addAll(this.driver.findElements(By.xpath("//*[@id='main']/table/tbody/tr/td[2]/table/tbody/tr[" + (j + 1) + "]/td[" + (i + 1)
                        + "]/table")));
            }
        }

        return zongoRooms;
    }

    private Collection<Calendar> searchOnDays() {
        final Collection<Calendar> searchOnDays = new HashSet<Calendar>();

        // final Range<Calendar> searchDayRange = getSearchRange();

        for (final Timeslot timeslot : this.zongobukkContext.getRequiredTimeslots()) {
            // if (searchDayRange.contains(timeslot.getStartDate())) {
            searchOnDays.add(DateUtil.truncateCalendar(timeslot.getStartDate()));
            // }
        }

        return searchOnDays;
    }

    // private Range<Calendar> getSearchRange() {
    // final Calendar upperLimit = Calendar.getInstance();
    // upperLimit.add(Calendar.DAY_OF_MONTH, this.bookPeriodInDay);
    //
    // return Range.closedOpen(DateUtil.truncateCalendar(Calendar.getInstance()),
    // DateUtil.truncateCalendar(upperLimit));
    // }

    private Calendar parseTime(final Calendar searchDay, final WebElement freeLink) throws ParseException {
        final String slotBookDate = freeLink.getText();

        log.trace("Timeslot found on {}", slotBookDate);

        final Calendar timeSlotCalendar = Calendar.getInstance();
        timeSlotCalendar.setTime(TIMESLOT_FORMAT.parse(slotBookDate));

        DateUtil.setDatePartOfCalendar(searchDay, timeSlotCalendar);

        return timeSlotCalendar;
    }

    // public void setBookPeriodInDay(final int bookPeriodInDay) {
    // this.bookPeriodInDay = bookPeriodInDay;
    // }

    public void setDaypickerLink(final String daypickerLink) {
        this.daypickerLink = daypickerLink;
    }

    public void setValidRoomNumbers(final int[] validRoomNumbers) {
        this.validRoomNumbers = validRoomNumbers;
    }

    public void setUrlBuilder(final UrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }

}
