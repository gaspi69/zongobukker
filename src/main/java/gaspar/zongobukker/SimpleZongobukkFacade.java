package gaspar.zongobukker;

import gaspar.web.WebManipulatorFacade;
import gaspar.zongobukker.bean.Timeslot;
import gaspar.zongobukker.bean.Timeslot.Status;
import gaspar.zongobukker.core.ZongoRoomBukker;
import gaspar.zongobukker.web.ZongobukkBookAction;
import gaspar.zongobukker.web.ZongobukkLoginAction;
import gaspar.zongobukker.web.ZongobukkLogoutAction;
import gaspar.zongobukker.web.ZongobukkSearchAction;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
public class SimpleZongobukkFacade implements WebManipulatorFacade<ZongobukkSession>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private ZongoRoomBukker zongoRoomBukker;

    public void run(final ZongobukkSession session) {
        if (session.getUserConfiguration().isProcessEnabled()) {
            try {
                init(session);
                report(session);

                login(session);
                report(session);

                search(session);
                report(session);

                calculate(session);
                report(session);

                modify(session);
                report(session);

                finalize(session);
                report(session);
            } catch (final RuntimeException e) {
                log.error("unhandled error", e);
            } finally {
                logout(session);
                report(session);
                logBookings(session);
            }
        } else {
            log.info("Process disabled");
        }
    }

    private void finalize(final ZongobukkSession session) {
        for (final Timeslot timeslot : session.getZongobukkContext().getRequiredTimeslots()) {
            timeslot.getComment().append(" - Processing finished");
        }
    }

    private void init(final ZongobukkSession session) {
        for (final Timeslot timeslot : session.getZongobukkContext().getRequiredTimeslots()) {
            timeslot.setStatus(Status.INITIALIZED);
            timeslot.getComment().append(" - Processing started");
        }
    }

    private void logBookings(final ZongobukkSession session) {
        ToStringBuilder.reflectionToString(session.getZongobukkContext().getRequiredTimeslots().toArray(), ToStringStyle.SIMPLE_STYLE);
    }

    @Override
    public void login(final ZongobukkSession session) {
        ((ZongobukkLoginAction) this.applicationContext.getBean("zongobukkLoginAction", session)).run();
    }

    @Override
    public void search(final ZongobukkSession session) {
        ((ZongobukkSearchAction) this.applicationContext.getBean("timeslotSearchAction", session)).run();
    }

    @Override
    public void calculate(final ZongobukkSession session) {
        this.zongoRoomBukker.bukkTimeslots(session.getZongobukkContext());
    }

    @Override
    public void modify(final ZongobukkSession session) {
        ((ZongobukkBookAction) this.applicationContext.getBean("zongobukkBookAction", session)).run();
    }

    @Override
    public void logout(final ZongobukkSession session) {
        ((ZongobukkLogoutAction) this.applicationContext.getBean("zongobukkLogoutAction", session)).run();
    }

    public void report(final ZongobukkSession session) {
        for (final Timeslot timeslot : session.getZongobukkContext().getRequiredTimeslots()) {
            session.getUserConfiguration().updateTimeslot(timeslot);
        }
    }

    public void setZongoRoomBukker(final ZongoRoomBukker zongoRoomBukker) {
        this.zongoRoomBukker = zongoRoomBukker;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}
