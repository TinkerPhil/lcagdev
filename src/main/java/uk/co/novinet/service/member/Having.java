package uk.co.novinet.service.member;

import java.util.List;

public class Having {
    private String sql;
    private List<Object> arguments;

    public Having(String sql, List<Object> arguments) {
        this.sql = sql;
        this.arguments = arguments;
    }

    public String getSql() {
        return sql;
    }

    public List<Object> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "Having{" +
                "sql='" + sql + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}
