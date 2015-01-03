package gaspar.zongobukker.core;

import gaspar.zongobukker.bean.PianoRoomPriorityComparator;
import gaspar.zongobukker.bean.Timeslot;
import gaspar.zongobukker.bean.TimeslotStartComparator;
import gaspar.zongobukker.bean.TimeslotStartPredicate;
import gaspar.zongobukker.bean.TimeslotStatusPredicate;
import gaspar.zongobukker.bean.ZongobukkContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

@Data
@Slf4j
public class PriorityRoomFinder implements ZongoRoomBukker {

    private PianoRoomPriorityComparator pianoRoomPriorityComparator;
    private TimeslotStartComparator timeslotStartComparator;

    @Override
    public void bukkTimeslots(final ZongobukkContext zongobukkContext) {
        final List<Timeslot> availableTimeslots = new ArrayList<Timeslot>(zongobukkContext.getCurrentTimeslots());
        Collections.sort(availableTimeslots, this.pianoRoomPriorityComparator);

        final List<Timeslot> allTimeslots = new ArrayList<Timeslot>(zongobukkContext.getRequiredTimeslots());
        Collections.sort(allTimeslots, this.timeslotStartComparator);

        checkTimeslots(availableTimeslots, allTimeslots);

        bookTimeslots(availableTimeslots, allTimeslots);
    }

    private void checkTimeslots(final List<Timeslot> availableTimeslots, final List<Timeslot> timeslotsToBook) {
        for (final Timeslot timeslotToBook : timeslotsToBook) {
            final Iterable<Timeslot> possibleTimeslotIterable = Iterables.filter(availableTimeslots, new TimeslotStartPredicate(timeslotToBook.getStartDate()));

            final Optional<Timeslot> alreadyBookedTimeslot = Iterables.tryFind(possibleTimeslotIterable, new TimeslotStatusPredicate(Timeslot.Status.MYBOOKING));
            if (alreadyBookedTimeslot.isPresent()) {
                timeslotToBook.setStatus(Timeslot.Status.MYBOOKING);
                timeslotToBook.getComment().append(" - Timeslot already booked at room #").append(alreadyBookedTimeslot.get().getRoomNumber());
                timeslotToBook.setRoomNumber(alreadyBookedTimeslot.get().getRoomNumber());

                log.info("Timeslot already booked for this person: {}", timeslotToBook);
            } else if (!Iterables.isEmpty(possibleTimeslotIterable)) {
                if (Iterables.all(possibleTimeslotIterable, new TimeslotStatusPredicate(Timeslot.Status.BOOKED))) {
                    timeslotToBook.getComment().append(" - all room booked by others :(");
                    timeslotToBook.setStatus(Timeslot.Status.BOOKED);

                    log.warn("Timeslot booked in all room booked by others: {}", timeslotToBook);
                } else if (Iterables.all(possibleTimeslotIterable, new TimeslotStatusPredicate(Timeslot.Status.TEMPORARILYBLOCKED))) {
                    timeslotToBook.getComment().append(" - all room temporarly unavailable :(");
                    timeslotToBook.setStatus(Timeslot.Status.TEMPORARILYBLOCKED);

                    log.warn("Timeslot temporarly unavailable all room: {}", timeslotToBook);
                }
            } else {
                timeslotToBook.getComment().append(" - Timeslot not available yet");
                timeslotToBook.setStatus(Timeslot.Status.FREE);

                log.info("Timeslot not available yet: {}", timeslotToBook);
            }
        }
    }

    private void bookTimeslots(final Collection<Timeslot> availableTimeslots, final Collection<Timeslot> workTimeslotsToBook) {
        for (final Timeslot workTimeslotToBook : workTimeslotsToBook) {
            if (Timeslot.Status.INITIALIZED.equals(workTimeslotToBook.getStatus())) {
                final Iterable<Timeslot> possibleTimeslotIterable = Iterables.filter(availableTimeslots,
                        new TimeslotStartPredicate(workTimeslotToBook.getStartDate()));

                final Optional<Timeslot> freeTimeslot = Iterables.tryFind(possibleTimeslotIterable, new TimeslotStatusPredicate(Timeslot.Status.FREE));
                if (freeTimeslot.isPresent()) {
                    workTimeslotToBook.getComment().append(" - Booking started at room #").append(freeTimeslot.get().getRoomNumber());
                    workTimeslotToBook.setStatus(Timeslot.Status.TO_BE_BOOKED);
                    workTimeslotToBook.setRoomNumber(freeTimeslot.get().getRoomNumber());
                } else {
                    throw new IllegalStateException("Invalid timeslot found");
                }
            }
        }
    }

}
