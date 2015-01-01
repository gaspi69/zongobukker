package gaspar.zongobukker.bean;

import java.util.Comparator;

public class PianoRoomPriorityComparator implements Comparator<Timeslot> {

    private int[] roomPriority;

    @Override
    public int compare(final Timeslot o1, final Timeslot o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }

        if (o1 == null) {
            return -1;
        }

        if (o2 == null) {
            return 1;
        }

        return roomPrioIndexOf(o1.getRoomNumber()) - roomPrioIndexOf(o2.getRoomNumber());
    }

    private int roomPrioIndexOf(final int elem) {
        if (this.roomPriority == null) {
            return -1;
        }

        for (int i = 0; i < this.roomPriority.length; i++) {
            if (this.roomPriority[i] == elem) {
                return i;
            }
        }

        return -1;
    }

    public void setRoomPriority(final int[] roomPriority) {
        this.roomPriority = roomPriority;
    }

}
