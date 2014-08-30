package gaspar.zongobukker;

import java.text.ParseException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Slf4j
public class Zongobukker {

    private Zongobukker() {
    }

    /**
     * Application's entry point.
     * 
     * @param args
     *            no function
     * @throws ParseException
     */
    public static void main(final String[] args) throws ParseException {
        ClassPathXmlApplicationContext context = null;

        try {
            context = new ClassPathXmlApplicationContext("applicationContext.xml");
            context.getBean(ZongobukkManager.class).makeBookings();
        } catch (final BeansException e) {
            log.error("Unable to initialize context", e);
        } catch (final RuntimeException e) {
            log.error("Unhandled/unexpected exception :(", e);
        } finally {
            if (context != null) {
                context.close();
            }
            log.info("Application finished");
        }

    }

}
