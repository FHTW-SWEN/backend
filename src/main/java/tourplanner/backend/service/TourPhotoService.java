package tourplanner.backend.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tourplanner.backend.dto.TourPhotoResponse;
import tourplanner.backend.persistence.entity.TourPhoto;
import tourplanner.backend.persistence.repository.TourPhotoRepository;
import tourplanner.backend.persistence.repository.TourRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Service
public class TourPhotoService {

    private static final Logger log = LogManager.getLogger(TourPhotoService.class);

    private static final long MAX_FILE_SIZE = 8L * 1024 * 1024; // 8 MB
    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final TourPhotoRepository tourPhotoRepository;
    private final TourRepository tourRepository;

    public TourPhotoService(TourPhotoRepository tourPhotoRepository, TourRepository tourRepository) {
        this.tourPhotoRepository = tourPhotoRepository;
        this.tourRepository = tourRepository;
    }

    public List<TourPhotoResponse> getPhotosForTour(Long tourId, Long userId) {
        assertTourBelongsToUser(tourId, userId);
        log.debug("Load Images for tourId={}", tourId);
        return tourPhotoRepository.findByTourIdOrderByUploadedAtDesc(tourId).stream()
                .map(this::toResponse)
                .toList();
    }

    public TourPhotoResponse uploadPhoto(Long tourId, Long userId, MultipartFile file, String caption) {
        assertTourBelongsToUser(tourId, userId);
        validateFile(file);

        try {
            TourPhoto photo = new TourPhoto(
                    tourId,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes(),
                    caption,
                    LocalDateTime.now()
            );
            TourPhoto saved = tourPhotoRepository.save(photo);
            log.info("Photo uploaded: tourId={}, photoId={}, size={} bytes", tourId, saved.getId(), file.getSize());
            return toResponse(saved);
        } catch (IOException e) {
            log.error("The photo could not be read: {}", e.getMessage());
            throw new IllegalArgumentException("The file could not be processed.");
        }
    }

    public boolean deletePhoto(Long tourId, Long photoId, Long userId) {
        assertTourBelongsToUser(tourId, userId);
        return tourPhotoRepository.findById(photoId)
                .filter(photo -> photo.getTourId().equals(tourId))
                .map(photo -> {
                    tourPhotoRepository.delete(photo);
                    log.info("Photo is deleted: tourId={}, photoId={}", tourId, photoId);
                    return true;
                })
                .orElse(false);
    }

    private void assertTourBelongsToUser(Long tourId, Long userId) {
        tourRepository.findByIdAndUserId(tourId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Tour not found or no access."));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("The file is too large (max. 8 MB).");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Just JPEG, PNG, WEBP or GIF allowed.");
        }
    }

    private TourPhotoResponse toResponse(TourPhoto photo) {
        String base64 = Base64.getEncoder().encodeToString(photo.getData());
        String dataUrl = "data:" + photo.getContentType() + ";base64," + base64;
        return new TourPhotoResponse(
                photo.getId(), photo.getTourId(), photo.getFileName(),
                photo.getCaption(), photo.getUploadedAt(), dataUrl
        );
    }
}