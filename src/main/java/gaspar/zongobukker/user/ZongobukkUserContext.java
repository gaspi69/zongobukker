package gaspar.zongobukker.user;

import gaspar.zongobukker.bean.Room;
import gaspar.zongobukker.bean.Timeslot;

import java.io.Serializable;
import java.util.Collection;

public interface ZongobukkUserContext extends Serializable {

    String getUsername();

    void setUsername(final String username);

    String getPassword();

    void setPassword(final String password);

    Collection<Timeslot> getRequiredTimeslots();

    Collection<Room> getRooms();

}
