package uk.co.novinet.service.member;

import java.time.Instant;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class SftpDocument {
    private String filename;
    private String path;
    private Instant uploadDate;

    public SftpDocument(String filename, String path, Instant uploadDate) {
        this.filename = filename;
        this.path = path;
        this.uploadDate = uploadDate;
    }

    public String getFilename() {
        return filename;
    }

    public String getPath() {
        return path;
    }

    public Instant getUploadDate() {
        return uploadDate;
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }
}
