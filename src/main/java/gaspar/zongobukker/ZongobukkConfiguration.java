package gaspar.zongobukker;

import gaspar.zongobukker.user.ZongobukkUserContext;
import lombok.Data;

import org.openqa.selenium.WebDriver;

@Data
public class ZongobukkConfiguration {

    private ZongobukkUserContext zongobukkUserContext;

    private WebDriver driver;

}
