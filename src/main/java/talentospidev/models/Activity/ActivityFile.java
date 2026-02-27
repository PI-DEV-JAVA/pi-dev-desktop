package talentospidev.models.Activity;

import java.time.LocalDateTime;

public class ActivityFile {
    private int id;
    private int activityId;
    private String fileName;
    private String filePath;
    private long fileSize;
    private String fileType;
    private LocalDateTime uploadedAt;

    public ActivityFile() {
    }

    public ActivityFile(int activityId, String fileName, String filePath, long fileSize, String fileType) {
        this.activityId = activityId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileType = fileType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getFormattedFileSize() {
        if (fileSize < 1024)
            return fileSize + " B";
        if (fileSize < 1024 * 1024)
            return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
    }
}