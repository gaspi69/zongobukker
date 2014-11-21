package gaspar.zongobukker.bean;

import java.util.Calendar;

import lombok.Data;

import org.apache.commons.lang3.time.DateFormatUtils;

@Data
public class Timeslot implements Cloneable {

    private Calendar startDate;

    private Status status = Status.UNKNOWN;

    // private String actionLink;

    public enum Status {
        FREE, MYBOOKING, UNKNOWN;
    }

    @Override
    protected Timeslot clone() {
        final Timeslot cloneTimeslot = new Timeslot();

        cloneTimeslot.startDate = Calendar.getInstance();
        cloneTimeslot.startDate.setTimeInMillis(this.startDate.getTimeInMillis());

        cloneTimeslot.setStatus(this.status);

        // cloneTimeslot.actionLink = this.actionLink;

        return cloneTimeslot;
    }

    @Override
    public String toString() {
        return "Timeslot [startDate=" + DateFormatUtils.ISO_DATETIME_FORMAT.format(this.startDate) + ", status=" + this.status + "]";
    }

}
