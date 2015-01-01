package gaspar.zongobukker.bean;


import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import lombok.Data;

@Data
public class ZongobukkContext implements Serializable {

    private static final long serialVersionUID = -2446150067196706590L;

    protected final Collection<Timeslot> requiredTimeslots = new HashSet<Timeslot>();

    protected final Collection<Timeslot> currentTimeslots = new HashSet<Timeslot>();

}
