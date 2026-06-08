/*package tourplanner.backend.controller;

import tourplanner.backend.persistence.entity.Tour;
import tourplanner.backend.dto.TourResponse;
import tourplanner.backend.service.TourService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tours")
@CrossOrigin(origins = "http://localhost:4200")
//@RequestMapping("/tours")
public class TourController {

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    @GetMapping
    public List<TourResponse> getAllTours() {
        return tourService.getAllTours();
    }

    @GetMapping("/search")
    public List<TourResponse> searchTours(@RequestParam(required = false) String q) {
        return tourService.searchTours(q);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourResponse> getTourById(@PathVariable Long id) {
        return tourService.getTourById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Tour> createTour(@RequestBody Tour tour) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tourService.createTour(tour));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tour> updateTour(@PathVariable Long id, @RequestBody Tour tour) {
        return tourService.updateTour(id, tour)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTour(@PathVariable Long id) {
        if (tourService.deleteTour(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}*/
package tourplanner.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tourplanner.backend.dto.TourResponse;
import tourplanner.backend.persistence.entity.Tour;
import tourplanner.backend.service.TourService;

import java.util.List;

@RestController
@RequestMapping("/api/tours")
@CrossOrigin(origins = "http://localhost:4200")
public class TourController {

    private static final Logger log = LogManager.getLogger(TourController.class);

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    @GetMapping
    public List<TourResponse> getAllTours(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.debug("GET /api/tours - userId={}", userId);
        return tourService.getAllToursByUser(userId);
    }

    @GetMapping("/search")
    public List<TourResponse> searchTours(@RequestParam(required = false) String q,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.debug("GET /api/tours/search?q={} - userId={}", q, userId);
        return tourService.searchTours(q, userId); // Service-Methode anpassen
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourResponse> getTourById(@PathVariable Long id,
                                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.debug("GET /api/tours/{} - userId={}", id, userId);
        return tourService.getTourByIdAndUser(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createTour(@RequestBody Tour tour, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("POST /api/tours - userId={}, Name: {}", userId, tour.getName());
        try {
            tour.setUserId(userId);
            Tour created = tourService.createTour(tour);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.warn("Tour konnte nicht erstellt werden: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTour(@PathVariable Long id, @RequestBody Tour tour,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("PUT /api/tours/{} - userId={}", id, userId);
        try {
            return tourService.updateTourForUser(id, tour, userId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            log.warn("Tour konnte nicht aktualisiert werden: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTour(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("DELETE /api/tours/{} - userId={}", id, userId);
        if (tourService.deleteTourForUser(id, userId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
