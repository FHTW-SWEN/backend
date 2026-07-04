package tourplanner.backend.dto;

import java.time.LocalDateTime;

public class TourPhotoResponse {

    private Long id;
    private Long tourId;
    private String fileName;
    private String caption;
    private LocalDateTime uploadedAt;
    /** Bild als Data-URL (data:image/...;base64,...) — direkt in <img src> nutzbar. */
    private String dataUrl;

    public TourPhotoResponse() {}

    public TourPhotoResponse(Long id, Long tourId, String fileName, String caption,
                             LocalDateTime uploadedAt, String dataUrl) {
        this.id = id;
        this.tourId = tourId;
        this.fileName = fileName;
        this.caption = caption;
        this.uploadedAt = uploadedAt;
        this.dataUrl = dataUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTourId() { return tourId; }
    public void setTourId(Long tourId) { this.tourId = tourId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getDataUrl() { return dataUrl; }
    public void setDataUrl(String dataUrl) { this.dataUrl = dataUrl; }
}
