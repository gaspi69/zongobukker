package gaspar.zongobukker;

import gaspar.web.WebManipulatorFacade;
import gaspar.zongobukker.bean.Timeslot;
import gaspar.zongobukker.core.ZongobukkFinder;
import gaspar.zongobukker.web.ZongobukkBookAction;
import gaspar.zongobukker.web.ZongobukkLoginAction;
import gaspar.zongobukker.web.ZongobukkLogoutAction;
import gaspar.zongobukker.web.ZongobukkSearchAction;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
public class SimpleZongobukkFacade implements WebManipulatorFacade<ZongobukkConfiguration>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private ZongobukkFinder zongobukkFinder;

    public void run(final ZongobukkConfiguration configuration) {
        try {
            login(configuration);

            search(configuration);
            calculate(configuration);
            modify(configuration);

            logBookings(configuration);
        } catch (final RuntimeException e) {
            log.error("unhandled error", e);
        } finally {
            logout(configuration);
        }
    }

    private void logBookings(final ZongobukkConfiguration configuration) {
        for (final Timeslot timeslot : configuration.getZongobukkUserContext().getRequiredTimeslots()) {
            if (Timeslot.Status.MYBOOKING.equals(timeslot.getStatus())) {
                log.info("Booking SUCCESSFULL: {}", timeslot);
            } else if (Timeslot.Status.UNKNOWN.equals(timeslot.getStatus())) {
                log.info("Booking SKIPPED: {}", timeslot);
            } else if (Timeslot.Status.FREE.equals(timeslot.getStatus())) {
                log.error("Booking still FREE: {}", timeslot);
            }
        }
    }

    @Override
    public void login(final ZongobukkConfiguration configuration) {
        ((ZongobukkLoginAction) this.applicationContext.getBean("zongobukkLoginAction", configuration)).run();
    }

    @Override
    public void search(final ZongobukkConfiguration configuration) {
        ((ZongobukkSearchAction) this.applicationContext.getBean("timeslotSearchAction", configuration)).run();
    }

    @Override
    public void calculate(final ZongobukkConfiguration configuration) {
        this.zongobukkFinder.findTimeslots(configuration.getZongobukkUserContext());
    }

    @Override
    public void modify(final ZongobukkConfiguration configuration) {
        ((ZongobukkBookAction) this.applicationContext.getBean("zongobukkBookAction", configuration)).run();
    }

    @Override
    public void logout(final ZongobukkConfiguration configuration) {
        ((ZongobukkLogoutAction) this.applicationContext.getBean("zongobukkLogoutAction", configuration)).run();
    }

    public void setZongobukkFinder(final ZongobukkFinder zongobukkFinder) {
        this.zongobukkFinder = zongobukkFinder;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
