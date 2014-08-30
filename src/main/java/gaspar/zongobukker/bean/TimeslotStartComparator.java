package gaspar.zongobukker.bean;

import java.util.Comparator;

public class TimeslotStartComparator implements Comparator<Timeslot> {

    @Override
    public int compare(final Timeslot o1, final Timeslot o2) {
        if ((o1 == null || o1.getStartDate() == null) && (o2 == null || o2.getStartDate() == null)) {
            return 0;
        }

        if (o1 == null || o1.getStartDate() == null) {
            return -1;
        }

        if (o2 == null || o2.getStartDate() == null) {
            return 1;
        }

        return Long.valueOf(o1.getStartDate().getTimeInMillis() - o2.getStartDate().getTimeInMillis()).intValue();
    }

}
