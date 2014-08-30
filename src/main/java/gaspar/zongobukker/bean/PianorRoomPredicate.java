package gaspar.zongobukker.bean;

import lombok.Data;

import com.google.common.base.Predicate;

@Data
public class PianorRoomPredicate implements Predicate<Room> {

    private int[] pianoRooms;

    @Override
    public boolean apply(final Room input) {
        if (input != null) {
            for (final int pianoRoom : this.pianoRooms) {
                if (pianoRoom == input.getNumber()) {
                    return true;
                }
            }
        }

        return false;
    }

}
