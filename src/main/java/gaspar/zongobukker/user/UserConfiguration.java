package gaspar.zongobukker.user;

import gaspar.google.data.GoogleTable;
import gaspar.zongobukker.bean.Timeslot;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Range;
import com.google.gdata.data.spreadsheet.Cell;
import com.google.gdata.data.spreadsheet.CellEntry;

@Slf4j
@Data
public class UserConfiguration implements Serializable {

    private static final long serialVersionUID = 5764663269391636109L;

    private final GoogleTable googleTable;

    private String timeslotFormat;

    public UserConfiguration(final GoogleTable googleTable) {
        super();
        this.googleTable = googleTable;
    }

    public boolean isProcessEnabled() {
        final String cellText = this.googleTable.getCellText(5, 2);
        return cellText != null && cellText.equalsIgnoreCase("BE");
    }

    public String getUsername() {
        return this.googleTable.getCellText(2, 1);
    }

    public String getPassword() {
        return this.googleTable.getCellText(2, 2);
    }

    public Calendar getLastUpdated() {
        final Calendar lastUpdateCalendar = Calendar.getInstance();

        try {
            lastUpdateCalendar.setTime(new SimpleDateFormat(this.timeslotFormat).parse(this.googleTable.getCellText(5, 1)));
        } catch (final ParseException e) {
            log.error("unable to parse last update date", e);
        }

        return lastUpdateCalendar;
    }

    private void updateLastUpdate() {
        this.googleTable.writeCellText(5, 1, new SimpleDateFormat(this.timeslotFormat).format(Calendar.getInstance().getTime()));
    }

    public Collection<Timeslot> loadTimeslots() {
        final DateFormat timeslotFormat = new SimpleDateFormat(this.timeslotFormat);

        final Collection<Timeslot> resultCollection = new ArrayList<Timeslot>();

        for (final CellEntry cellEntry : this.googleTable.getCells(Range.singleton(1), Range.atLeast(4))) {
            final Cell timeCell = cellEntry.getCell();
            try {
                final Timeslot timeslot = new Timeslot();

                final Calendar date = Calendar.getInstance();

                date.setTimeInMillis(timeslotFormat.parse(timeCell.getValue()).getTime());

                timeslot.setStartDate(date);
                timeslot.getComment().append("Init OK");

                resultCollection.add(timeslot);
            } catch (final ParseException e) {
                log.error("Unable to parse date: {}, correct format: {}", timeCell.getValue(), this.timeslotFormat);
            }
        }

        return resultCollection;
    }

    public void updateTimeslot(final Timeslot timeslot) {

        final int rowIndexByTime = findRowIndexByTime(timeslot.getStartDate());

        if (rowIndexByTime > 0) {
            String status = null;

            switch (timeslot.getStatus()) {
            case BOOKED:
                status = "WARNING";
                break;
            case FREE:
                status = "FREE";
                break;
            case MYBOOKING:
                status = "OK";
                break;
            case TO_BE_BOOKED:
            case INITIALIZED:
                status = "PROCESSING";
                break;
            case UNKNOWN:
            case TEMPORARILYBLOCKED:
                status = "ERROR";
                break;
            }

            this.googleTable.writeCellText(2, rowIndexByTime, status);
            this.googleTable.writeCellText(3, rowIndexByTime, timeslot.getComment().toString());
        }

        updateLastUpdate();
    }

    private int findRowIndexByTime(final Calendar calendar) {
        final DateFormat timeslotFormat = new SimpleDateFormat(this.timeslotFormat);

        for (final CellEntry cellEntry : this.googleTable.getCells(Range.singleton(1), Range.atLeast(4))) {
            final Cell timeCell = cellEntry.getCell();

            try {
                if (timeslotFormat.parse(timeCell.getValue()).equals(calendar.getTime())) {
                    return timeCell.getRow();
                }
            } catch (final ParseException e) {
                log.error("unable to parse date", e);
            }
        }

        return -1;
    }
}
