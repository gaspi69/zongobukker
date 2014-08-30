package gaspar.zongobukker.core;

import gaspar.zongobukker.bean.PianoRoomPriorityComparator;
import gaspar.zongobukker.bean.PianorRoomPredicate;
import gaspar.zongobukker.bean.Room;
import gaspar.zongobukker.bean.Timeslot;
import gaspar.zongobukker.bean.TimeslotStartComparator;
import gaspar.zongobukker.bean.ZongobukkException;
import gaspar.zongobukker.user.ZongobukkUserContext;
import gaspar.zongobukker.util.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Collections2;
import com.google.common.collect.Range;

@Data
@Slf4j
public class PianoRoomFinder implements ZongobukkFinder {

    private PianorRoomPredicate pianorRoomPredicate;
    private PianoRoomPriorityComparator pianoRoomPriorityComparator;
    private TimeslotStartComparator timeslotStartComparator = new TimeslotStartComparator();

    private int maxRetryCount;

    private int bookPeriodInDay;

    @Override
    public void findTimeslots(final ZongobukkUserContext zongobukkUserContext) {
        final List<Room> availableRooms = new ArrayList<Room>(Collections2.filter(zongobukkUserContext.getRooms(), this.pianorRoomPredicate));
        Collections.sort(availableRooms, this.pianoRoomPriorityComparator);

        final List<Timeslot> timeslotsToBook = new ArrayList<Timeslot>(zongobukkUserContext.getRequiredTimeslots());
        Collections.sort(timeslotsToBook, this.timeslotStartComparator);

        removeInvalidTimeSlots(availableRooms, timeslotsToBook);

        int count = 0;
        while (hasEmptySlot(timeslotsToBook) && count < this.maxRetryCount) {
            for (int i = 0; i < timeslotsToBook.size(); i++) {
                final List<Timeslot> workTimeslots = timeslotsToBook.subList(i, timeslotsToBook.size());

                for (final Room room : availableRooms) {
                    matchAndSetTimeslots(room, workTimeslots);
                }
            }

            count++;
        }

        if (hasEmptySlot(timeslotsToBook)) {
            throw new ZongobukkException("Proper booking not found");
        }
    }

    private void removeInvalidTimeSlots(final List<Room> availableRooms, final List<Timeslot> timeslotsToBook) {

        final Calendar upperLimit = Calendar.getInstance();
        upperLimit.add(Calendar.DAY_OF_MONTH, this.bookPeriodInDay);

        final Range<Calendar> searchDayRange = Range.closedOpen(DateUtil.truncateCalendar(Calendar.getInstance()), DateUtil.truncateCalendar(upperLimit));

        for (final Iterator<Timeslot> iterator = timeslotsToBook.iterator(); iterator.hasNext();) {
            final Timeslot timeslot = iterator.next();

            if (!searchDayRange.contains(timeslot.getStartDate())) {
                timeslot.setStatus(Timeslot.Status.SKIP);
                iterator.remove();
            } else {
                for (final Room room : availableRooms) {
                    final Timeslot alreadyBooked = findTimeslotByStart(room.getTimeslots(), timeslot.getStartDate());
                    if (alreadyBooked != null && Timeslot.Status.MYBOOKING.equals(alreadyBooked.getStatus())) {
                        log.warn("Timeslot already booked: {}", alreadyBooked);

                        timeslot.setStatus(Timeslot.Status.MYBOOKING);
                        iterator.remove();
                    }
                }
            }
        }
    }

    private boolean hasEmptySlot(final List<Timeslot> timeslotsToBook) {
        for (final Timeslot timeslot : timeslotsToBook) {
            if (timeslot.getActionLink() == null) {
                return true;
            }
        }

        return false;
    }

    private void matchAndSetTimeslots(final Room room, final List<Timeslot> workTimeslots) {
        for (final Timeslot timeslot : workTimeslots) {
            final Timeslot availableTimeslot = findTimeslotByStart(room.getTimeslots(), timeslot.getStartDate());
            if (availableTimeslot != null) {
                timeslot.setActionLink(availableTimeslot.getActionLink());
            }
        }
    }

    private Timeslot findTimeslotByStart(final Collection<Timeslot> timeslots, final Calendar calendar) {
        for (final Timeslot timeslot : timeslots) {
            if (timeslot.getStartDate().equals(calendar)) {
                return timeslot;
            }
        }

        return null;
    }

}
