package gaspar.zongobukker.user;

import gaspar.google.data.GoogleTable;
import gaspar.zongobukker.bean.Timeslot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Range;

@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class TableInitializationUserContext extends ZongobukkUserContextSupport {

    private static final long serialVersionUID = -5515439255288055548L;

    private final GoogleTable googleTable;

    private String timeslotFormat;

    public TableInitializationUserContext(final GoogleTable googleTable) {
        super();
        this.googleTable = googleTable;
    }

    public void init() {
        this.username = this.googleTable.getCell(2, 1);
        this.password = this.googleTable.getCell(2, 2);

        final DateFormat timeslotFormat = new SimpleDateFormat(this.timeslotFormat);

        for (final String timeslotString : this.googleTable.getCells(Range.singleton(1), Range.atLeast(4))) {
            try {
                final Timeslot timeslot = new Timeslot();

                final Calendar date = Calendar.getInstance();
                date.setTimeInMillis(timeslotFormat.parse(timeslotString).getTime());
                timeslot.setStartDate(date);

                this.requiredTimeslots.add(timeslot);
            } catch (final ParseException e) {
                log.error("Unable to parse date: {}, correct format: {}", timeslotString, this.timeslotFormat);
            }
        }
    }

}
