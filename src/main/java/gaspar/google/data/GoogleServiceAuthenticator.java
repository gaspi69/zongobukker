package gaspar.google.data;

import lombok.Data;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.util.AuthenticationException;

@Data
public class GoogleServiceAuthenticator {

    private SpreadsheetService spreadsheetService;

    private String username;
    private String password;

    public void init() throws AuthenticationException {
        this.spreadsheetService = new SpreadsheetService(getClass().toString());
        this.spreadsheetService.setProtocolVersion(SpreadsheetService.Versions.V3);

        this.spreadsheetService.setUserCredentials(this.username, this.password);
    }

    public SpreadsheetService getSpreadsheetService() {
        return this.spreadsheetService;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

}
