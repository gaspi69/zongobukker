package gaspar.web;

import java.net.MalformedURLException;
import java.net.URL;

import lombok.Data;

@Data
public class UrlBuilder {

    public URL buildUrl(final String urlTemplate, final Object... params) throws MalformedURLException {
        return new URL(String.format(urlTemplate, params).toString());
    }
}
