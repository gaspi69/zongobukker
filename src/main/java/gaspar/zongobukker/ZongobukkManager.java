package gaspar.zongobukker;

import gaspar.google.data.GoogleTable;
import gaspar.web.FirefoxWebInstance;
import gaspar.zongobukker.user.TableInitializationUserContext;

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
        for (final String userString : this.userIndexTable.getCells(Range.singleton(1), Range.atLeast(1))) {
            try {
                final GoogleTable userConfigTable = initUserConfigTable(userString);

                final FirefoxWebInstance firefoxWebInstance = this.applicationContext.getBean(FirefoxWebInstance.class);

                final ZongobukkConfiguration configuration = initConfiguration(userConfigTable, firefoxWebInstance);

                this.zongobukkFacade.run(configuration);
            } catch (final Exception e) {
                log.error("Booking failed for: " + userString, e);
            }
        }
    }

    private ZongobukkConfiguration initConfiguration(final GoogleTable userConfigTable, final FirefoxWebInstance firefoxWebInstance) {
        final ZongobukkConfiguration configuration = new ZongobukkConfiguration();
        final TableInitializationUserContext userContext = (TableInitializationUserContext) this.applicationContext.getBean("userContext", userConfigTable);
        userContext.init();
        configuration.setZongobukkUserContext(userContext);
        configuration.setDriver(firefoxWebInstance.getDriver());
        return configuration;
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
