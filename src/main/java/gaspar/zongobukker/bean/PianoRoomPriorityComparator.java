package gaspar.zongobukker.bean;

import java.util.Comparator;

public class PianoRoomPriorityComparator implements Comparator<Room> {

    private int[] roomPriority;

    @Override
    public int compare(final Room o1, final Room o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }

        if (o1 == null) {
            return -1;
        }

        if (o2 == null) {
            return 1;
        }

        return roomPrioIndexOf(o1.getNumber()) - roomPrioIndexOf(o2.getNumber());
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
