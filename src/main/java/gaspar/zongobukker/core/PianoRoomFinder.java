package gaspar.zongobukker.core;

import gaspar.zongobukker.bean.PianoRoomPriorityComparator;
import gaspar.zongobukker.bean.Timeslot;
import gaspar.zongobukker.bean.Timeslot.Status;
import gaspar.zongobukker.bean.TimeslotStartAndStatusPredicate;
import gaspar.zongobukker.bean.TimeslotStartComparator;
import gaspar.zongobukker.bean.TimeslotStartPredicate;
import gaspar.zongobukker.bean.TimeslotStatusPredicate;
import gaspar.zongobukker.bean.ZongobukkContext;
import gaspar.zongobukker.util.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.time.DateFormatUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Range;

@Data
@Slf4j
public class PianoRoomFinder {

    private PianoRoomPriorityComparator pianoRoomPriorityComparator;
    private TimeslotStartComparator timeslotStartComparator;

    private int maxRetryCount;

    private int bookPeriodInDay;

    public void findTimeslots(final ZongobukkContext zongobukkContext) {
        final List<Timeslot> availableTimeslots = new ArrayList<Timeslot>(zongobukkContext.getCurrentTimeslots());
        Collections.sort(availableTimeslots, this.pianoRoomPriorityComparator);

        final List<Timeslot> timeslotsToBook = new ArrayList<Timeslot>(zongobukkContext.getRequiredTimeslots());
        Collections.sort(timeslotsToBook, this.timeslotStartComparator);

        filterInvalidTimeSlots(availableTimeslots, timeslotsToBook);

        int count = 0;
        while (hasEmptySlot(timeslotsToBook) && count < this.maxRetryCount) {
            for (int i = 0; i < timeslotsToBook.size(); i++) {
                final List<Timeslot> workTimeslotsToBook = timeslotsToBook.subList(i, timeslotsToBook.size());

                matchAndSetTimeslots(availableTimeslots, workTimeslotsToBook);
            }

            count++;
        }

        // if (hasEmptySlot(timeslotsToBook)) {
        // throw new ZongobukkException("Proper booking not found");
        // }
    }

    private void filterInvalidTimeSlots(final List<Timeslot> availableTimeslots, final List<Timeslot> timeslotsToBook) {
        final Range<Calendar> searchDayRange = getSearchRange();

        for (final Timeslot timeslotToBook : timeslotsToBook) {
            if (!searchDayRange.contains(timeslotToBook.getStartDate())) {
                timeslotToBook.setStatus(Timeslot.Status.UNKNOWN);
                timeslotToBook.getComment().append(" - Timeslot out of booking period");

                log.info("Timeslot out of booking period: {}", timeslotToBook);
            } else {
                final boolean alreadyBooked = Iterables.any(availableTimeslots,
                        new TimeslotStartAndStatusPredicate(timeslotToBook.getStartDate(), Timeslot.Status.MYBOOKING));
                if (alreadyBooked) {
                    timeslotToBook.setStatus(Timeslot.Status.MYBOOKING);
                    timeslotToBook.getComment().append(" - Timeslot already booked");

                    log.info("Timeslot already booked for this person: {}", timeslotToBook);
                }
            }
        }
    }

    private Range<Calendar> getSearchRange() {
        final Calendar upperLimit = Calendar.getInstance();
        upperLimit.add(Calendar.DAY_OF_MONTH, this.bookPeriodInDay);

        return Range.closedOpen(DateUtil.truncateCalendar(Calendar.getInstance()), DateUtil.truncateCalendar(upperLimit));
    }

    private boolean hasEmptySlot(final Collection<Timeslot> timeslotsToBook) {
        return Iterables.any(timeslotsToBook, new TimeslotStatusPredicate(Timeslot.Status.INITIALIZED));
    }

    private void matchAndSetTimeslots(final Collection<Timeslot> availableTimeslots, final Collection<Timeslot> workTimeslotsToBook) {
        for (final Timeslot workTimeslotToBook : workTimeslotsToBook) {
            try {
                final Timeslot availableTimeslot = Iterables.find(availableTimeslots, new TimeslotStartPredicate(workTimeslotToBook.getStartDate()));
                if (Timeslot.Status.FREE.equals(availableTimeslot.getStatus())) {
                    workTimeslotToBook.getComment().append(" - Booking started ...");
                    workTimeslotToBook.setStatus(Timeslot.Status.TO_BE_BOOKED);
                    workTimeslotToBook.setRoomNumber(availableTimeslot.getRoomNumber());
                } else if (!Timeslot.Status.MYBOOKING.equals(availableTimeslot.getStatus())) {
                    workTimeslotToBook.getComment().append(" - Free timeslot not found");
                    workTimeslotToBook.setStatus(Timeslot.Status.UNKNOWN);
                }
            } catch (final NoSuchElementException e) {
                workTimeslotToBook.setStatus(Status.UNKNOWN);
                workTimeslotToBook.getComment().append(" - Timeslot booking not available");
                log.error("could not found timeslot: {}", DateFormatUtils.ISO_DATETIME_FORMAT.format(workTimeslotToBook.getStartDate()));
            }
        }
    }

}
