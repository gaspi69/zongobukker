package gaspar.zongobukker.bean;

import java.util.Calendar;

import lombok.Data;

import org.apache.commons.lang3.time.DateFormatUtils;

@Data
public class Timeslot implements Cloneable {

    private Calendar startDate;

    private Status status = Status.UNKNOWN;

    private int roomNumber;

    private final StringBuffer comment = new StringBuffer();

    public enum Status {
        FREE, BOOKED, MYBOOKING, TEMPORARILYBLOCKED, INITIALIZED, TO_BE_BOOKED, UNKNOWN;
    }

    @Override
    protected Timeslot clone() {
        final Timeslot cloneTimeslot = new Timeslot();

        cloneTimeslot.startDate = Calendar.getInstance();
        cloneTimeslot.startDate.setTimeInMillis(this.startDate.getTimeInMillis());

        cloneTimeslot.status = this.status;

        cloneTimeslot.roomNumber = this.roomNumber;

        return cloneTimeslot;
    }

    @Override
    public String toString() {
        return "Timeslot [roomNumber=" + this.roomNumber + ", startDate=" + DateFormatUtils.ISO_DATETIME_FORMAT.format(this.startDate) + ", status="
                + this.status + "]";
    }

}
