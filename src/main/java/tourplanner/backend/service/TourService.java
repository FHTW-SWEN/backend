/*package tourplanner.backend.service;

import tourplanner.backend.persistence.entity.Tour;
import tourplanner.backend.persistence.repository.TourRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TourService {

    private final TourRepository tourRepository;

    public TourService(TourRepository tourRepository) {
        this.tourRepository = tourRepository;
    }

    public List<Tour> getAllTours() {
        return tourRepository.findAll();
    }

    public Optional<Tour> getTourById(Long id) {
        return tourRepository.findById(id);
    }

    public Tour createTour(Tour tour) {
        return tourRepository.save(tour);
    }

    public Optional<Tour> updateTour(Long id, Tour tour) {
        return tourRepository.update(id, tour);
    }

    public boolean deleteTour(Long id) {
        return tourRepository.delete(id);
    }
}*/
package tourplanner.backend.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import tourplanner.backend.dto.TourResponse;
import tourplanner.backend.dto.ors.RouteInfo;
import tourplanner.backend.persistence.entity.Tour;
import tourplanner.backend.persistence.entity.TourLog;
import tourplanner.backend.persistence.repository.TourLogRepository;
import tourplanner.backend.persistence.repository.TourRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Business-Layer Service für Tour-Operationen.
 *
 * Beim Erstellen einer Tour wird automatisch ORS aufgerufen,
 * um Distanz, Zeit und Routenkoordinaten zu berechnen.
 * Der User gibt nur name, description, from, to, transportType an.
 */
@Service
public class TourService {

    private static final Logger log = LogManager.getLogger(TourService.class);

    private final TourRepository tourRepository;
    private final TourLogRepository tourLogRepository;
    private final RouteService routeService;

    public TourService(TourRepository tourRepository, TourLogRepository tourLogRepository, RouteService routeService) {
        this.tourRepository = tourRepository;
        this.tourLogRepository = tourLogRepository;
        this.routeService = routeService;
    }

    public List<TourResponse> getAllTours() {
        log.debug("Lade alle Tours aus der Datenbank");
        return tourRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<TourResponse> getTourById(Long id) {
        log.debug("Suche Tour mit ID: {}", id);
        return tourRepository.findById(id).map(this::toResponse);
    }

    public List<TourResponse> searchTours(String query) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isBlank()) {
            return getAllTours();
        }

        return tourRepository.findAll().stream()
                .map(tour -> {
                    List<TourLog> logs = tourLogRepository.findByTourId(tour.getId());
                    TourResponse response = toResponse(tour, logs);
                    return new SearchCandidate(response, buildSearchText(response, logs));
                })
                .filter(candidate -> candidate.searchText().contains(normalizedQuery))
                .map(SearchCandidate::tour)
                .toList();
    }

    /**
     * Erstellt eine neue Tour.
     * ORS wird aufgerufen um distance, estimatedTime und routeCoordinates zu befüllen.
     * Diese Felder vom Client werden ignoriert und durch ORS-Daten ersetzt.
     */
    public Tour createTour(Tour tour) {
        log.info("Erstelle neue Tour: {} ({} -> {})", tour.getName(), tour.getFrom(), tour.getTo());

        // ORS aufrufen → Distanz, Zeit und Routenkoordinaten holen
        RouteInfo routeInfo = routeService.calculateRoute(
                tour.getFrom(),
                tour.getTo(),
                tour.getTransportType()
        );

        // ORS-Ergebnisse in die Tour schreiben
        tour.setDistance(routeInfo.getDistanceKm());
        tour.setEstimatedTime(routeInfo.getEstimatedTimeMinutes());
        tour.setRouteCoordinates(routeInfo.getRouteCoordinatesJson());

        Tour saved = tourRepository.save(tour);
        log.info("Tour gespeichert mit ID: {}, Distanz: {}km, Zeit: {}min",
                saved.getId(), saved.getDistance(), saved.getEstimatedTime());
        return saved;
    }

    /**
     * Aktualisiert eine bestehende Tour.
     * Falls from, to oder transportType geändert wurden, wird ORS erneut aufgerufen.
     */
    public Optional<Tour> updateTour(Long id, Tour updated) {
        log.info("Aktualisiere Tour mit ID: {}", id);

        return tourRepository.findById(id).map(existing -> {
            boolean routeChanged = !existing.getFrom().equals(updated.getFrom())
                    || !existing.getTo().equals(updated.getTo())
                    || !existing.getTransportType().equals(updated.getTransportType());

            existing.setName(updated.getName());
            existing.setDescription(updated.getDescription());
            existing.setFrom(updated.getFrom());
            existing.setTo(updated.getTo());
            existing.setTransportType(updated.getTransportType());
            existing.setImageUrl(updated.getImageUrl());

            if (routeChanged) {
                log.info("Route hat sich geändert, rufe ORS erneut auf");
                RouteInfo routeInfo = routeService.calculateRoute(
                        updated.getFrom(),
                        updated.getTo(),
                        updated.getTransportType()
                );
                existing.setDistance(routeInfo.getDistanceKm());
                existing.setEstimatedTime(routeInfo.getEstimatedTimeMinutes());
                existing.setRouteCoordinates(routeInfo.getRouteCoordinatesJson());
            }

            return tourRepository.save(existing);
        });
    }

    public boolean deleteTour(Long id) {
        log.info("Lösche Tour mit ID: {}", id);
        if (tourRepository.existsById(id)) {
            tourRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private TourResponse toResponse(Tour tour) {
        return toResponse(tour, tourLogRepository.findByTourId(tour.getId()));
    }

    private TourResponse toResponse(Tour tour, List<TourLog> logs) {
        int popularity = Math.toIntExact(tourLogRepository.countByTourId(tour.getId()));
        int childFriendliness = calculateChildFriendliness(logs);
        return new TourResponse(tour, popularity, childFriendliness);
    }

    private int calculateChildFriendliness(List<TourLog> logs) {
        if (logs.isEmpty()) {
            return 0;
        }

        double average = logs.stream()
                .mapToInt(this::calculateLogChildFriendliness)
                .average()
                .orElse(0);
        return (int) Math.round(average);
    }

    private int calculateLogChildFriendliness(TourLog log) {
        return (difficultyScore(log.getDifficulty())
                + timeScore(log.getTotalTime())
                + distanceScore(log.getTotalDistance())) / 3;
    }

    private int difficultyScore(Integer difficulty) {
        if (difficulty == null) return 0;
        return Math.max(1, 6 - difficulty);
    }

    private int timeScore(Integer totalTime) {
        if (totalTime == null) return 0;
        if (totalTime <= 60) return 5;
        if (totalTime <= 120) return 4;
        if (totalTime <= 180) return 3;
        if (totalTime <= 240) return 2;
        return 1;
    }

    private int distanceScore(Double totalDistance) {
        if (totalDistance == null) return 0;
        if (totalDistance <= 3) return 5;
        if (totalDistance <= 5) return 4;
        if (totalDistance <= 10) return 3;
        if (totalDistance <= 15) return 2;
        return 1;
    }

    private String buildSearchText(TourResponse tour, List<TourLog> logs) {
        StringBuilder text = new StringBuilder();
        append(text, tour.getName());
        append(text, tour.getDescription());
        append(text, tour.getFrom());
        append(text, tour.getTo());
        append(text, tour.getTransportType());
        append(text, tour.getDistance());
        append(text, tour.getEstimatedTime());
        append(text, tour.getRouteCoordinates());
        append(text, tour.getImageUrl());
        append(text, tour.getPopularity());
        append(text, tour.getChildFriendliness());
        append(text, "popularity:" + tour.getPopularity());
        append(text, "childfriendliness:" + tour.getChildFriendliness());

        for (TourLog log : logs) {
            append(text, log.getDateTime());
            append(text, log.getComment());
            append(text, log.getDifficulty());
            append(text, log.getTotalDistance());
            append(text, log.getTotalTime());
            append(text, log.getRating());
        }
        return normalize(text.toString());
    }

    private void append(StringBuilder text, Object value) {
        if (value != null) {
            text.append(' ').append(value);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record SearchCandidate(TourResponse tour, String searchText) {}
}
