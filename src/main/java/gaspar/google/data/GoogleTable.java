package gaspar.google.data;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Predicate;
import com.google.common.collect.Range;
import com.google.gdata.data.spreadsheet.Cell;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

@Slf4j
@Data
public class GoogleTable implements Serializable {

    private static final long serialVersionUID = -4011799595440248057L;

    private GoogleServiceAuthenticator authenticator;

    private String spreadsheetTitle;
    private String worksheetTitle;

    private URL feedUrl;

    private CellFeed cellFeed;

    public void init() throws IOException, ServiceException {
        final SpreadsheetFeed spreadsheetFeed = this.authenticator.getSpreadsheetService().getFeed(this.feedUrl, SpreadsheetFeed.class);

        final List<SpreadsheetEntry> spreadsheets = spreadsheetFeed.getEntries();
        for (final SpreadsheetEntry spreadsheet : spreadsheets) {
            if (this.spreadsheetTitle.equals(spreadsheet.getTitle().getPlainText())) {
                log.debug("Spreadsheet found: {}", this.spreadsheetTitle);

                for (final WorksheetEntry worksheetEntry : spreadsheet.getWorksheets()) {
                    if (this.worksheetTitle.equals(worksheetEntry.getTitle().getPlainText())) {
                        log.debug("Worksheet found: {}", this.worksheetTitle);

                        this.cellFeed = this.authenticator.getSpreadsheetService().getFeed(worksheetEntry.getCellFeedUrl(), CellFeed.class);
                    }
                }
            }
        }

        if (this.cellFeed == null) {
            throw new IllegalArgumentException("Cells not found: " + this.worksheetTitle + " in " + this.spreadsheetTitle);
        }
    }

    private List<String> getCells(final Predicate<Cell> predicate) {
        final List<String> resultStrings = new ArrayList<String>();

        for (final CellEntry cellEntry : this.cellFeed.getEntries()) {
            final Cell cell = cellEntry.getCell();

            log.trace("Cell loaded: {}", cell.getValue());

            if (predicate.apply(cell)) {
                resultStrings.add(cell.getValue());
            }
        }

        return resultStrings;
    }

    public String getCell(final int col, final int row) {
        final List<String> cells = getCells(new Predicate<Cell>() {
            @Override
            public boolean apply(@Nullable final Cell cell) {
                return cell.getCol() == col && cell.getRow() == row;
            }
        });

        if (cells.isEmpty()) {
            return null;
        } else {
            return cells.get(0);
        }
    }

    public List<String> getCells(final Range<Integer> colRange, final Range<Integer> rowRange) {
        return getCells(new Predicate<Cell>() {
            @Override
            public boolean apply(@Nullable final Cell cell) {
                return colRange.contains(cell.getCol()) && rowRange.contains(cell.getRow());
            }
        });
    }

}
