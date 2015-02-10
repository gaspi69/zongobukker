package gaspar.zongobukker.core;

import gaspar.zongobukker.bean.PianoRoomPriorityComparator;
import gaspar.zongobukker.bean.Timeslot;
import gaspar.zongobukker.bean.TimeslotRoomPredicate;
import gaspar.zongobukker.bean.TimeslotStartComparator;
import gaspar.zongobukker.bean.TimeslotStartPredicate;
import gaspar.zongobukker.bean.TimeslotStatusPredicate;
import gaspar.zongobukker.bean.ZongobukkContext;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Data
@Slf4j
public class GroupingSlotRoomFinder implements ZongoRoomBukker {

    private final class TimeslotToRoomNumberFunction implements Function<Timeslot, Integer> {
        @Override
        @Nullable
        public Integer apply(@Nullable final Timeslot input) {
            return input.getRoomNumber();
        }
    }

    private PianoRoomPriorityComparator pianoRoomPriorityComparator;
    private TimeslotStartComparator timeslotStartComparator;

    @Override
    public void bukkTimeslots(final ZongobukkContext zongobukkContext) {
        final List<Timeslot> allCurrentTimeslots = new ArrayList<Timeslot>(zongobukkContext.getCurrentTimeslots());
        Collections.sort(allCurrentTimeslots, this.pianoRoomPriorityComparator);

        final List<Timeslot> allBookingTimeslots = new ArrayList<Timeslot>(zongobukkContext.getRequiredTimeslots());
        Collections.sort(allBookingTimeslots, this.timeslotStartComparator);

        checkTimeslots(allCurrentTimeslots, allBookingTimeslots);

        final List<Timeslot> freeTimeslots = Lists.newArrayList(Iterables.filter(allCurrentTimeslots, new TimeslotStatusPredicate(Timeslot.Status.FREE)));

        while (hasInitializedTimeslot(allBookingTimeslots)) {
            final List<Timeslot> bookingTimeslots = Lists.newArrayList(Iterables.filter(allBookingTimeslots,
                    new TimeslotStatusPredicate(Timeslot.Status.INITIALIZED)));

            for (int i = bookingTimeslots.size(); i >= 0 && Iterables.all(bookingTimeslots, new TimeslotStatusPredicate(Timeslot.Status.INITIALIZED)); i--) {
                bookTimeslotsToSameRoom(freeTimeslots, bookingTimeslots.subList(0, i));
            }
        }
    }

    private void checkTimeslots(final List<Timeslot> availableTimeslots, final List<Timeslot> timeslotsToBook) {
        final Calendar now = Calendar.getInstance();

        for (final Timeslot timeslotToBook : timeslotsToBook) {
            if (now.after(timeslotToBook.getStartDate())) {
                timeslotToBook.getComment().append(" - Timeslot in the past");
                timeslotToBook.setStatus(Timeslot.Status.FREE);

                log.info("Timeslot in the past: {}", timeslotToBook);
            } else {
                final Iterable<Timeslot> exactTimeslots = Iterables.filter(availableTimeslots, new TimeslotStartPredicate(timeslotToBook.getStartDate()));

                final Optional<Timeslot> bookedTimeslot = Iterables.tryFind(exactTimeslots, new TimeslotStatusPredicate(Timeslot.Status.MYBOOKING));
                if (bookedTimeslot.isPresent()) {
                    timeslotToBook.setStatus(Timeslot.Status.MYBOOKING);
                    timeslotToBook.getComment().append(" - Timeslot already booked at room #").append(bookedTimeslot.get().getRoomNumber());
                    timeslotToBook.setRoomNumber(bookedTimeslot.get().getRoomNumber());

                    log.info("Timeslot already booked for this person: {}", timeslotToBook);
                } else {
                    final Optional<Timeslot> freeTimeslot = Iterables.tryFind(exactTimeslots, new TimeslotStatusPredicate(Timeslot.Status.FREE));
                    if (!freeTimeslot.isPresent()) {
                        timeslotToBook.getComment().append(" - Free timeslot not found");
                        timeslotToBook.setStatus(Timeslot.Status.UNKNOWN);

                        log.warn("Free timeslot not found: {}", timeslotToBook);

                        if (!Iterables.isEmpty(exactTimeslots)) {
                            if (Iterables.all(exactTimeslots, new TimeslotStatusPredicate(Timeslot.Status.BOOKED))) {
                                timeslotToBook.getComment().append(" - All room booked by others :(");
                                timeslotToBook.setStatus(Timeslot.Status.BOOKED);

                                log.warn("Timeslot booked in all room booked by others: {}", timeslotToBook);
                            } else if (Iterables.all(exactTimeslots, new TimeslotStatusPredicate(Timeslot.Status.TEMPORARILYBLOCKED))) {
                                timeslotToBook.getComment().append(" - All room temporarly unavailable :(");
                                timeslotToBook.setStatus(Timeslot.Status.TEMPORARILYBLOCKED);

                                log.warn("Timeslot temporarly unavailable all room: {}", timeslotToBook);
                            }
                        } else {
                            timeslotToBook.getComment().append(" - Timeslot not available");
                            timeslotToBook.setStatus(Timeslot.Status.FREE);

                            log.info("Timeslot not available: {}", timeslotToBook);
                        }
                    }
                }
            }
        }
    }

    private boolean isFreeTimeslot(final Collection<Timeslot> timeslots, final Calendar startDate, final int roomNumber) {
        final Iterable<Timeslot> possibleTimeslotIterable = Iterables.filter(timeslots, new TimeslotStartPredicate(startDate));

        return Iterables.any(possibleTimeslotIterable, new TimeslotRoomPredicate(roomNumber));
    }

    private boolean hasInitializedTimeslot(final Collection<Timeslot> timeslotsToBook) {
        return Iterables.any(timeslotsToBook, new TimeslotStatusPredicate(Timeslot.Status.INITIALIZED));
    }

    private void bookTimeslotsToSameRoom(final List<Timeslot> freeTimeslots, final List<Timeslot> workTimeslotsToBook) {
        final Iterable<Integer> freeRoomNumbers = Iterables.transform(freeTimeslots, new TimeslotToRoomNumberFunction());

        for (final Integer roomNumber : Sets.newLinkedHashSet(freeRoomNumbers)) {
            if (isAllTimeslotMatching(freeTimeslots, workTimeslotsToBook, roomNumber)) {
                for (final Timeslot workTimeslotToBook : workTimeslotsToBook) {
                    workTimeslotToBook.getComment().append(" - Booking started at room #").append(roomNumber);
                    workTimeslotToBook.setStatus(Timeslot.Status.TO_BE_BOOKED);
                    workTimeslotToBook.setRoomNumber(roomNumber);
                }

                return;
            }
        }
    }

    private boolean isAllTimeslotMatching(final List<Timeslot> freeTimeslots, final List<Timeslot> workTimeslotsToBook, final Integer roomNumber) {
        for (final Timeslot workTimeslotToBook : workTimeslotsToBook) {
            if (!isFreeTimeslot(freeTimeslots, workTimeslotToBook.getStartDate(), roomNumber)) {
                return false;
            }
        }

        return true;
    }

}
