package gaspar.zongobukker.bean;

import com.google.common.base.Predicate;

public class TimeslotRoomPredicate implements Predicate<Timeslot> {

    final int roomNumber;

    public TimeslotRoomPredicate(final int roomNumber) {
        super();

        this.roomNumber = roomNumber;
    }

    @Override
    public boolean apply(final Timeslot input) {
        return input != null && this.roomNumber == input.getRoomNumber();
    }

}
