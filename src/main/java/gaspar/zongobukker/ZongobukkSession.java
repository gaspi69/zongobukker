package gaspar.zongobukker;

import gaspar.zongobukker.bean.ZongobukkContext;
import gaspar.zongobukker.user.UserConfiguration;
import lombok.Data;

import org.openqa.selenium.WebDriver;

@Data
public class ZongobukkSession {

    private UserConfiguration userConfiguration;

    private ZongobukkContext zongobukkContext;

    private WebDriver driver;

}
