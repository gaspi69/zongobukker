package gaspar.zongobukker.bean;

import java.util.Collection;
import java.util.HashSet;

import lombok.Data;

@Data
public class Room implements Cloneable {

    private int number;

    private RoomType roomType;

    private final Collection<Timeslot> timeslots = new HashSet<Timeslot>();

    @Override
    public Room clone() {
        final Room cloneRoom = new Room();

        cloneRoom.number = this.number;

        cloneRoom.roomType = this.roomType;

        for (final Timeslot timeslot : this.timeslots) {
            cloneRoom.timeslots.add(timeslot.clone());
        }

        return cloneRoom;

    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Room other = (Room) obj;
        if (this.number != other.number) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.number;
        return result;
    }

}
