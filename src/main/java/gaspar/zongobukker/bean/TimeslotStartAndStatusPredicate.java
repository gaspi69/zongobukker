package gaspar.zongobukker.bean;

import java.util.Calendar;

import javax.annotation.Nonnull;

import com.google.common.base.Predicate;

public class TimeslotStartAndStatusPredicate implements Predicate<Timeslot> {

    private final Calendar startDate;
    private final Timeslot.Status status;

    public TimeslotStartAndStatusPredicate(@Nonnull final Calendar startDate, @Nonnull final Timeslot.Status status) {
        super();

        this.startDate = startDate;
        this.status = status;
    }

    @Override
    public boolean apply(final Timeslot input) {
        return input != null && this.startDate.equals(input.getStartDate()) && this.status.equals(input.getStatus());
    }

}
