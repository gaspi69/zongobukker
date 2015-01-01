package gaspar.google.data;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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

    private final class PositionRangePredicate implements Predicate<CellEntry> {
        private final Range<Integer> colRange;
        private final Range<Integer> rowRange;

        private PositionRangePredicate(final Range<Integer> colRange, final Range<Integer> rowRange) {
            this.colRange = colRange;
            this.rowRange = rowRange;
        }

        @Override
        public boolean apply(@Nullable final CellEntry cellEntry) {
            return this.colRange.contains(cellEntry.getCell().getCol()) && this.rowRange.contains(cellEntry.getCell().getRow());
        }
    }

    private final class PositionPredicate implements Predicate<CellEntry> {
        private final int row;
        private final int col;

        private PositionPredicate(final int col, final int row) {
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean apply(@Nullable final CellEntry cellEntry) {
            return cellEntry.getCell().getCol() == this.col && cellEntry.getCell().getRow() == this.row;
        }
    }

    private static final long serialVersionUID = -4011799595440248057L;

    private GoogleServiceAuthenticator authenticator;

    private String spreadsheetTitle;
    private String worksheetTitle;

    private URL spreadsheetFeedUrl;

    private URL cellFeedUrl;

    public void init() throws IOException, ServiceException {
        final SpreadsheetFeed spreadsheetFeed = this.authenticator.getSpreadsheetService().getFeed(this.spreadsheetFeedUrl, SpreadsheetFeed.class);

        final List<SpreadsheetEntry> spreadsheets = spreadsheetFeed.getEntries();
        for (final SpreadsheetEntry spreadsheet : spreadsheets) {
            if (this.spreadsheetTitle.equals(spreadsheet.getTitle().getPlainText())) {
                log.debug("Spreadsheet found: {}", this.spreadsheetTitle);

                for (final WorksheetEntry worksheetEntry : spreadsheet.getWorksheets()) {
                    if (this.worksheetTitle.equals(worksheetEntry.getTitle().getPlainText())) {
                        log.debug("Worksheet found: {}", this.worksheetTitle);

                        this.cellFeedUrl = worksheetEntry.getCellFeedUrl();
                    }
                }
            }
        }

        if (this.cellFeedUrl == null) {
            throw new IllegalArgumentException("Cells not found: " + this.worksheetTitle + " in " + this.spreadsheetTitle);
        }

        log.debug("CellFeed URL is {}", this.cellFeedUrl);
    }

    private CellFeed getCellFeed() {
        try {
            return this.authenticator.getSpreadsheetService().getFeed(this.cellFeedUrl, CellFeed.class);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        } catch (final ServiceException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<String> getCellsText(final Predicate<CellEntry> predicate) {
        final List<String> resultStrings = new ArrayList<String>();

        for (final CellEntry cellEntry : Iterables.filter(getCellFeed().getEntries(), predicate)) {
            final Cell cell = cellEntry.getCell();

            log.trace("Cell loaded: {}", cell.getValue());

            resultStrings.add(cell.getValue());
        }

        return resultStrings;
    }

    public List<CellEntry> getCells(final Range<Integer> colRange, final Range<Integer> rowRange) {
        return Lists.newArrayList(Iterables.filter(getCellFeed().getEntries(), (new PositionRangePredicate(colRange, rowRange))));
    }

    public CellEntry getCell(final int col, final int row) {
        final Iterator<CellEntry> cells = Iterables.filter(getCellFeed().getEntries(), new PositionPredicate(col, row)).iterator();

        if (cells.hasNext()) {
            return cells.next();
        }

        return null;
    }

    public String getCellText(final int col, final int row) {
        final List<String> cells = getCellsText(new PositionPredicate(col, row));

        if (cells.isEmpty()) {
            return null;
        } else {
            return cells.get(0);
        }
    }

    public List<String> getCellsText(final Range<Integer> colRange, final Range<Integer> rowRange) {
        return getCellsText(new PositionRangePredicate(colRange, rowRange));
    }

    public void writeCellText(final int col, final int row, final String text) {
        final CellEntry cellEntry = getCell(col, row);

        if (cellEntry != null) {
            cellEntry.changeInputValueLocal(text);
            try {
                cellEntry.update();
            } catch (final IOException e) {
                log.error("", e);
            } catch (final ServiceException e) {
                log.error("", e);
            }
        } else {
            log.warn("unable to write cell @(c={}, r={})", col, row);
        }
    }

}
