package tourplanner.backend.controller;

import tourplanner.backend.persistence.entity.TourLog;
import tourplanner.backend.service.TourLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "http://localhost:4200")
public class TourLogController {

    private final TourLogService tourLogService;

    public TourLogController(TourLogService tourLogService) {
        this.tourLogService = tourLogService;
    }

    @GetMapping
    public List<TourLog> getAllTourLogs(@RequestParam(required = false) Long tourId) {
        if (tourId != null) {
            return tourLogService.getTourLogsByTourId(tourId);
        }
        return tourLogService.getAllTourLogs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourLog> getTourLogById(@PathVariable Long id) {
        return tourLogService.getTourLogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TourLog> createTourLog(@RequestBody TourLog tourLog) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tourLogService.createTourLog(tourLog));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourLog> updateTourLog(@PathVariable Long id, @RequestBody TourLog tourLog) {
        return tourLogService.updateTourLog(id, tourLog)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTourLog(@PathVariable Long id) {
        if (tourLogService.deleteTourLog(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
