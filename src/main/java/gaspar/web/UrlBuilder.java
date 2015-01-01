package gaspar.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class UrlBuilder {

    public URL buildUrl(final String urlTemplate, final Object... params) throws MalformedURLException {
        final List<String> paramStrings = new ArrayList<String>();
        if (params != null) {
            for (final Object param : params) {
                paramStrings.add(String.valueOf(param));
            }
        }

        return new URL(String.format(urlTemplate, paramStrings.toArray()).toString());
    }
}
