package tourplanner.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tourplanner.backend.dto.TourPhotoResponse;
import tourplanner.backend.service.TourPhotoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tours/{tourId}/photos")
@CrossOrigin(origins = "http://localhost:4200")
public class TourPhotoController {

    private static final Logger log = LogManager.getLogger(TourPhotoController.class);

    private final TourPhotoService tourPhotoService;

    public TourPhotoController(TourPhotoService tourPhotoService) {
        this.tourPhotoService = tourPhotoService;
    }

    @GetMapping
    public ResponseEntity<?> getPhotos(@PathVariable Long tourId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.debug("GET /api/tours/{}/photos - userId={}", tourId, userId);
        try {
            List<TourPhotoResponse> photos = tourPhotoService.getPhotosForTour(tourId, userId);
            return ResponseEntity.ok(photos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPhoto(@PathVariable Long tourId,
                                         @RequestParam("file") MultipartFile file,
                                         @RequestParam(value = "caption", required = false) String caption,
                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("POST /api/tours/{}/photos - userId={}, fileName={}", tourId, userId, file.getOriginalFilename());
        try {
            TourPhotoResponse created = tourPhotoService.uploadPhoto(tourId, userId, file, caption);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.warn("Foto-Upload fehlgeschlagen: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<?> deletePhoto(@PathVariable Long tourId, @PathVariable Long photoId,
                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("DELETE /api/tours/{}/photos/{} - userId={}", tourId, photoId, userId);
        try {
            if (tourPhotoService.deletePhoto(tourId, photoId, userId)) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}