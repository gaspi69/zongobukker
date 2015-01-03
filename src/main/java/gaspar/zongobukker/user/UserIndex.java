package gaspar.zongobukker.user;

import gaspar.google.data.GoogleTable;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Range;

@Component
@Scope(value = "prototype")
public class UserIndex implements Serializable {

    private static final long serialVersionUID = 5658653446939545481L;

    private final GoogleTable googleTable;

    public UserIndex(final GoogleTable googleTable) {
        super();
        this.googleTable = googleTable;
    }

    public Collection<String> getUserNames() {
        return this.googleTable.getCellsText(Range.singleton(1), Range.atLeast(1));
    }

}
