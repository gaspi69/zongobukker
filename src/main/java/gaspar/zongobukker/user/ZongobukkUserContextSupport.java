package gaspar.zongobukker.user;

import gaspar.zongobukker.bean.Room;
import gaspar.zongobukker.bean.Timeslot;

import java.util.Collection;
import java.util.HashSet;

import lombok.Data;

@Data
public abstract class ZongobukkUserContextSupport implements ZongobukkUserContext {

    private static final long serialVersionUID = -2446150067196706590L;

    protected String username;

    protected String password;

    protected final Collection<Timeslot> requiredTimeslots = new HashSet<Timeslot>();

    protected final Collection<Room> rooms = new HashSet<Room>();

}
