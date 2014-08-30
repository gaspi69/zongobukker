package gaspar.zongobukker.web;

import gaspar.web.WebAction;
import gaspar.zongobukker.ZongobukkConfiguration;
import gaspar.zongobukker.bean.Room;
import gaspar.zongobukker.bean.RoomType;
import gaspar.zongobukker.bean.Timeslot;
import gaspar.zongobukker.bean.ZongobukkException;
import gaspar.zongobukker.user.ZongobukkUserContext;
import gaspar.zongobukker.util.DateUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.google.common.collect.Range;

@Slf4j
public class ZongobukkSearchAction extends WebAction {

    private static final DateFormat TIMESLOT_FORMAT = new SimpleDateFormat("hh:mm");

    private final ZongobukkUserContext zongobukkUserContext;

    private int bookPeriodInDay;

    private ZongobukkSearchAction(final ZongobukkConfiguration configuration) {
        super(configuration.getDriver());
        this.zongobukkUserContext = configuration.getZongobukkUserContext();
    }

    @Override
    public void run() {
        for (final Calendar searchDay : searchOnDays()) {
            try {
                searchByDay(searchDay);
            } catch (final RuntimeException e) {
                log.error("unhandled exception, when searching for " + ToStringBuilder.reflectionToString(searchDay, ToStringStyle.SIMPLE_STYLE), e);
            }
        }

    }

    private void searchByDay(final Calendar searchDay) {
        new ZongobukkSelectDayAction(this.driver, searchDay).run();

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

    private void searchTimeslotsByDayAndRoom(final Calendar searchDay, final WebElement tableRoom) {
        final String roomString = tableRoom.findElement(By.tagName("strong")).getText();

        log.debug("Room found: {}", roomString);

        Room room = findRoomByNumber(Integer.valueOf(roomString));

        if (room == null) {
            room = new Room();
            room.setNumber(Integer.valueOf(roomString));

            room.setRoomType(findRoomType(tableRoom));

            this.zongobukkUserContext.getRooms().add(room);
        }

        parseBookingLinks(searchDay, tableRoom, room, Timeslot.Status.FREE);
        parseBookingLinks(searchDay, tableRoom, room, Timeslot.Status.MYBOOKING);
    }

    private void parseBookingLinks(final Calendar searchDay, final WebElement tableRoom, final Room room, final Timeslot.Status status) {
        for (final WebElement tdElement : tableRoom.findElements(By.className(status.toString().toLowerCase()))) {
            final WebElement timeslotLink = tdElement.findElement(By.tagName("a"));

            try {
                final Timeslot timeslot = parseTimeslot(searchDay, timeslotLink);

                timeslot.setStatus(status);

                room.getTimeslots().add(timeslot);

                log.trace("Timeslot succesfully added: {}", timeslot);
            } catch (final ParseException e) {
                log.error("Timeslot cannot parsed: {}", timeslotLink, e);
            }
        }
    }

    private RoomType findRoomType(final WebElement tableRoom) {
        RoomType roomType = null;

        for (final WebElement imgElement : tableRoom.findElements(By.tagName("img"))) {
            final String altAttribute = imgElement.getAttribute("alt");
            if (altAttribute == "Zongora") {
                roomType = RoomType.PIANO;
            } else if (altAttribute == "Pianínó") {
                roomType = RoomType.PIANINO;
            }
        }

        return roomType;
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

        final Calendar upperLimit = Calendar.getInstance();
        upperLimit.add(Calendar.DAY_OF_MONTH, this.bookPeriodInDay);

        final Range<Calendar> searchDayRange = Range.closedOpen(DateUtil.truncateCalendar(Calendar.getInstance()), DateUtil.truncateCalendar(upperLimit));

        for (final Timeslot timeslot : this.zongobukkUserContext.getRequiredTimeslots()) {
            if (searchDayRange.contains(timeslot.getStartDate())) {
                searchOnDays.add(DateUtil.truncateCalendar(timeslot.getStartDate()));
            }
        }

        return searchOnDays;
    }

    private Room findRoomByNumber(final int roomNumber) {
        for (final Room currentRoom : this.zongobukkUserContext.getRooms()) {
            if (roomNumber == currentRoom.getNumber()) {
                return currentRoom;
            }
        }

        return null;
    }

    private Timeslot parseTimeslot(final Calendar searchDay, final WebElement freeLink) throws ParseException {
        final String slotBookDate = freeLink.getText();
        final String slotBookLink = freeLink.getAttribute("href");

        log.trace("Free booking link found on {} with link: {}", slotBookDate, slotBookLink);

        final Timeslot timeslot = new Timeslot();

        final Calendar timeSlotCalendar = Calendar.getInstance();
        timeSlotCalendar.setTime(TIMESLOT_FORMAT.parse(slotBookDate));

        DateUtil.setDatePartOfCalendar(searchDay, timeSlotCalendar);

        timeslot.setStartDate(timeSlotCalendar);
        timeslot.setActionLink(slotBookLink);

        return timeslot;
    }

    public void setBookPeriodInDay(final int bookPeriodInDay) {
        this.bookPeriodInDay = bookPeriodInDay;
    }

}
