package gaspar.zongobukker;

import gaspar.google.data.GoogleTable;
import gaspar.web.BrowserInstance;
import gaspar.web.HtmlUnitWebInstance;
import gaspar.zongobukker.bean.ZongobukkContext;
import gaspar.zongobukker.user.UserConfiguration;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Range;
import com.google.gdata.util.ServiceException;

@Slf4j
public class ZongobukkManager implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private GoogleTable userIndexTable;

    private SimpleZongobukkFacade zongobukkFacade;

    public void makeBookings() {
        for (final String userString : this.userIndexTable.getCellsText(Range.singleton(1), Range.atLeast(1))) {
            try {
                final GoogleTable userConfigTable = initUserConfigTable(userString);

                final BrowserInstance browserInstance = this.applicationContext.getBean(HtmlUnitWebInstance.class);

                final ZongobukkSession session = initSession(userConfigTable, browserInstance);

                this.zongobukkFacade.run(session);

                browserInstance.destroy();
            } catch (final Exception e) {
                log.error("Booking failed for: " + userString, e);
            }
        }
    }

    private ZongobukkSession initSession(final GoogleTable userConfigTable, final BrowserInstance browserInstance) {
        final ZongobukkSession session = new ZongobukkSession();

        final UserConfiguration userConfiguration = (UserConfiguration) this.applicationContext.getBean("userConfiguration", userConfigTable);
        session.setUserConfiguration(userConfiguration);

        final ZongobukkContext userContext = new ZongobukkContext();
        userContext.getRequiredTimeslots().addAll(session.getUserConfiguration().loadTimeslots());

        session.setZongobukkContext(userContext);
        session.setDriver(browserInstance.getDriver());
        return session;
    }

    private GoogleTable initUserConfigTable(final String userString) throws IOException, ServiceException {
        final GoogleTable userConfigTable = this.applicationContext.getBean("googleTable", GoogleTable.class);
        userConfigTable.setSpreadsheetTitle(userString.toUpperCase());
        userConfigTable.setWorksheetTitle(userString.toUpperCase());
        userConfigTable.init();
        return userConfigTable;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setUserIndexTable(final GoogleTable userIndexTable) {
        this.userIndexTable = userIndexTable;
    }

    public void setZongobukkFacade(final SimpleZongobukkFacade zongobukkFacade) {
        this.zongobukkFacade = zongobukkFacade;
    }

}
