/*package tourplanner.backend.service;

<<<<<<< HEAD
=======
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

@Service
public class TourService {

    private static final Logger log = LogManager.getLogger(TourService.class);

    private final TourRepository tourRepository;
    private final TourLogRepository tourLogRepository;
    private final RouteService routeService;

    public TourService(TourRepository tourRepository, TourLogRepository tourLogRepository,
                       RouteService routeService) {
        this.tourRepository = tourRepository;
        this.tourLogRepository = tourLogRepository;
        this.routeService = routeService;
    }

    public List<TourResponse> getAllToursByUser(Long userId) {
        log.debug("Lade alle Tours für userId={}", userId);
        return tourRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<TourResponse> getTourByIdAndUser(Long id, Long userId) {
        log.debug("Suche Tour id={} für userId={}", id, userId);
        return tourRepository.findByIdAndUserId(id, userId).map(this::toResponse);
    }

    public List<TourResponse> searchTours(String query, Long userId) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isBlank()) {
            return getAllToursByUser(userId);
        }

        return tourRepository.findByUserId(userId).stream()
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
     */
    public Tour createTour(Tour tour) {
        log.info("Erstelle neue Tour: {} ({} -> {}) für userId={}",
                tour.getName(), tour.getFrom(), tour.getTo(), tour.getUserId());

        tour.setId(null);

        RouteInfo routeInfo = routeService.calculateRoute(
                tour.getFrom(), tour.getTo(), tour.getTransportType()
        );
        tour.setDistance(routeInfo.getDistanceKm());
        tour.setEstimatedTime(routeInfo.getEstimatedTimeMinutes());
        tour.setRouteCoordinates(routeInfo.getRouteCoordinatesJson());

        Tour saved = tourRepository.save(tour);
        log.info("Tour gespeichert: id={}, {}km, {}min", saved.getId(), saved.getDistance(), saved.getEstimatedTime());
        return saved;
    }

    /**
     * Aktualisiert eine Tour — nur wenn sie dem User gehört.
     * ORS wird erneut aufgerufen falls from/to/transportType geändert wurden.
     */
    public Optional<Tour> updateTourForUser(Long id, Tour updated, Long userId) {
        log.info("Aktualisiere Tour id={} für userId={}", id, userId);

        return tourRepository.findByIdAndUserId(id, userId).map(existing -> {
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
                log.info("Route geändert, rufe ORS erneut auf");
                RouteInfo routeInfo = routeService.calculateRoute(
                        updated.getFrom(), updated.getTo(), updated.getTransportType()
                );
                existing.setDistance(routeInfo.getDistanceKm());
                existing.setEstimatedTime(routeInfo.getEstimatedTimeMinutes());
                existing.setRouteCoordinates(routeInfo.getRouteCoordinatesJson());
            }

            return tourRepository.save(existing);
        });
    }

    public boolean deleteTourForUser(Long id, Long userId) {
        log.info("Lösche Tour id={} für userId={}", id, userId);
        return tourRepository.findByIdAndUserId(id, userId).map(tour -> {
            tourRepository.delete(tour);
            return true;
        }).orElse(false);
    }

    // --- Helper ---

    private TourResponse toResponse(Tour tour) {
        return toResponse(tour, tourLogRepository.findByTourId(tour.getId()));
    }

    private TourResponse toResponse(Tour tour, List<TourLog> logs) {
        int popularity = Math.toIntExact(tourLogRepository.countByTourId(tour.getId()));
        int childFriendliness = calculateChildFriendliness(logs);
        return new TourResponse(tour, popularity, childFriendliness);
    }

    private int calculateChildFriendliness(List<TourLog> logs) {
        if (logs.isEmpty()) return 0;
        double average = logs.stream()
                .mapToDouble(this::calculateLogChildFriendliness)
                .average()
                .orElse(0);
        return (int) Math.round(average);
    }

    private double calculateLogChildFriendliness(TourLog log) {
        return (scoreDifficulty(log.getDifficulty())
                + scoreTotalTime(log.getTotalTime())
                + scoreTotalDistance(log.getTotalDistance())) / 3.0;
    }

    private int scoreDifficulty(Integer difficulty) {
        if (difficulty == null) return 1;
        return Math.max(1, Math.min(5, 6 - difficulty));
    }

    private int scoreTotalTime(Integer totalTime) {
        if (totalTime == null) return 1;
        if (totalTime <= 60) return 5;
        if (totalTime <= 120) return 4;
        if (totalTime <= 180) return 3;
        if (totalTime <= 240) return 2;
        return 1;
    }

    private int scoreTotalDistance(Double totalDistance) {
        if (totalDistance == null) return 1;
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
        append(text, "popularity:" + tour.getPopularity());
        append(text, "childfriendliness:" + tour.getChildFriendliness());

        for (TourLog l : logs) {
            append(text, l.getDateTime());
            append(text, l.getComment());
            append(text, l.getDifficulty());
            append(text, l.getTotalDistance());
            append(text, l.getTotalTime());
            append(text, l.getRating());
        }
        return normalize(text.toString());
    }

    private void append(StringBuilder text, Object value) {
        if (value != null) text.append(' ').append(value);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record SearchCandidate(TourResponse tour, String searchText) {}
}
