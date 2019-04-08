package uk.co.novinet.service.audit;

import java.time.Instant;

public class AuditEvent {
    private Long id;
    private Instant date;
    private Long userId;
    private String eventName;
    private String eventParameters;

    public AuditEvent(Long id, Instant date, Long userId, String eventName, String eventParameters) {
        this.id = id;
        this.date = date;
        this.userId = userId;
        this.eventName = eventName;
        this.eventParameters = eventParameters;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setEventParameters(String eventParameters) {
        this.eventParameters = eventParameters;
    }

    public Long getId() {
        return id;
    }

    public Instant getDate() {
        return date;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventParameters() {
        return eventParameters;
    }
}
