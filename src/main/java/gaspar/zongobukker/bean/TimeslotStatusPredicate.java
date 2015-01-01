package gaspar.zongobukker.bean;

import javax.annotation.Nonnull;

import com.google.common.base.Predicate;

public class TimeslotStatusPredicate implements Predicate<Timeslot> {
    final Timeslot.Status status;

    public TimeslotStatusPredicate(@Nonnull final Timeslot.Status status) {
        super();

        this.status = status;
    }

    @Override
    public boolean apply(final Timeslot input) {
        return input != null && this.status.equals(input.getStatus());
    }
}
