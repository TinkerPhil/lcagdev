package uk.co.novinet.rest;

public class PaginationRequest {
    private long current;
    private long rowCount;

    public void setCurrent(long current) {
        this.current = current;
    }

    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    public long getCurrent() {
        return current;
    }

    public long getRowCount() {
        return rowCount;
    }

    @Override
    public String toString() {
        return "PaginationRequest{" +
                "current=" + current +
                ", rowCount=" + rowCount +
                '}';
    }
}
