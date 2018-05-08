package uk.co.novinet.rest;

import java.util.List;

public class DataContainer {
    private long page; // the 1-based number of the current page
    private long rowNum; // the page size - the maximal number of records in a page (the last page can contains less records)
    private long records; // total records (on all pages) in the grid
    private long total;
    private List<? extends Object> rows;

    public DataContainer(long page, long rowNum, long records, long total, List<? extends Object> rows) {
        this.page = page;
        this.rowNum = rowNum;
        this.records = records;
        this.total = total;
        this.rows = rows;
    }

    public long getPage() {
        return page;
    }

    public long getRowNum() {
        return rowNum;
    }

    public long getRecords() {
        return records;
    }

    public List<? extends Object> getRows() {
        return rows;
    }

    public long getTotal() {
        return total;
    }
}
