import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Tester {

    /**
     * @param args
     */
    public static void main(final String[] args) {
        try {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
            simpleDateFormat.parse("2014.03.02. 22:00:00");
        } catch (final ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
