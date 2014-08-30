package gaspar.zongobukker.util;

import java.util.Calendar;

public abstract class DateUtil {

    private DateUtil() {
    }

    public static Calendar truncateCalendar(final Calendar calendar) {
        if (calendar == null) {
            return null;
        }

        final Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(calendar.getTimeInMillis());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    };

    public static void setDatePartOfCalendar(final Calendar sourceCalendar, final Calendar destinationCalendar) {
        if (sourceCalendar == null || destinationCalendar == null) {
            return;
        }

        destinationCalendar.set(sourceCalendar.get(Calendar.YEAR), sourceCalendar.get(Calendar.MONTH), sourceCalendar.get(Calendar.DAY_OF_MONTH));
    };

}
