package gaspar.zongobukker.bean;

import java.util.Calendar;

import com.google.common.base.Predicate;

public class TimeslotStartPredicate implements Predicate<Timeslot> {

    private final Calendar startDate;

    public TimeslotStartPredicate(final Calendar startDate) {
        super();

        if (startDate == null) {
            throw new IllegalArgumentException("startdate is null");
        }

        this.startDate = startDate;
    }

    @Override
    public boolean apply(final Timeslot input) {
        return input != null && this.startDate.equals(input.getStartDate());
    }

}
