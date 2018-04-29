package uk.co.novinet.rest;

import java.util.List;

public class DataContainer {
    private long current;
    private long rowCount;
    private long total;
    private List<? extends Object> rows;

    public DataContainer(long current, long rowCount, long total, List<? extends Object> rows) {
        this.current = current;
        this.rowCount = rowCount;
        this.total = total;
        this.rows = rows;
    }

    public long getCurrent() {
        return current;
    }

    public long getRowCount() {
        return rowCount;
    }

    public long getTotal() {
        return total;
    }

    public List<? extends Object> getRows() {
        return rows;
    }
}
