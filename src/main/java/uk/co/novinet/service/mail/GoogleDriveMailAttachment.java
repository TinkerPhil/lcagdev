package uk.co.novinet.service.mail;

public class GoogleDriveMailAttachment {
    private String googleDriveAttachmentId;
    private String attachmentFilename;
    private String attachmentContentType;

    public GoogleDriveMailAttachment(String googleDriveAttachmentId, String attachmentFilename, String attachmentContentType) {
        this.googleDriveAttachmentId = googleDriveAttachmentId;
        this.attachmentFilename = attachmentFilename;
        this.attachmentContentType = attachmentContentType;
    }

    public String getGoogleDriveAttachmentId() {
        return googleDriveAttachmentId;
    }

    public String getAttachmentFilename() {
        return attachmentFilename;
    }

    public String getAttachmentContentType() {
        return attachmentContentType;
    }
}
