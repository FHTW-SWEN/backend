package tourplanner.backend.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import tourplanner.backend.dto.TourDataExport;
import tourplanner.backend.dto.TourResponse;
import tourplanner.backend.dto.ors.RouteInfo;
import tourplanner.backend.persistence.entity.Tour;
import tourplanner.backend.persistence.entity.TourLog;
import tourplanner.backend.persistence.repository.TourLogRepository;
import tourplanner.backend.persistence.repository.TourRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

        List<Tour> userTours = tourRepository.findByUserId(userId);
        Set<Long> persistedMatches = tourRepository.searchPersistedFields(userId, normalizedQuery).stream()
                .map(Tour::getId)
                .collect(Collectors.toSet());

        List<Long> tourIds = userTours.stream().map(Tour::getId).toList();
        if (!tourIds.isEmpty()) {
            persistedMatches.addAll(tourLogRepository.findMatchingTourIds(tourIds, normalizedQuery));
        }

        return userTours.stream()
                .map(this::toResponse)
                .filter(tour -> persistedMatches.contains(tour.getId()) || matchesComputedAttributes(tour, normalizedQuery))
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

    public TourDataExport exportTourData(Long userId) {
        List<TourDataExport.ExportedTour> tours = tourRepository.findByUserId(userId).stream()
                .map(tour -> new TourDataExport.ExportedTour(
                        tour.getName(),
                        tour.getDescription(),
                        tour.getFrom(),
                        tour.getTo(),
                        tour.getTransportType(),
                        tour.getDistance(),
                        tour.getEstimatedTime(),
                        tour.getRouteCoordinates(),
                        tour.getImageUrl(),
                        tourLogRepository.findByTourId(tour.getId()).stream()
                                .map(log -> new TourDataExport.ExportedTourLog(
                                        log.getDateTime(),
                                        log.getComment(),
                                        log.getDifficulty(),
                                        log.getTotalDistance(),
                                        log.getTotalTime(),
                                        log.getRating()
                                ))
                                .toList()
                ))
                .toList();
        return new TourDataExport(tours);
    }

    public TourDataExport.ImportResult importTourData(TourDataExport data, Long userId) {
        if (data == null || data.tours() == null) {
            throw new IllegalArgumentException("Import data must contain tours");
        }

        int importedTours = 0;
        int importedLogs = 0;
        for (TourDataExport.ExportedTour exportedTour : data.tours()) {
            validateImportedTour(exportedTour);

            Tour tour = new Tour();
            tour.setUserId(userId);
            tour.setName(exportedTour.name());
            tour.setDescription(exportedTour.description());
            tour.setFrom(exportedTour.from());
            tour.setTo(exportedTour.to());
            tour.setTransportType(exportedTour.transportType());
            tour.setDistance(exportedTour.distance());
            tour.setEstimatedTime(exportedTour.estimatedTime());
            tour.setRouteCoordinates(exportedTour.routeCoordinates());
            tour.setImageUrl(exportedTour.imageUrl());

            Tour savedTour = tourRepository.save(tour);
            importedTours++;

            if (exportedTour.logs() == null) {
                continue;
            }
            for (TourDataExport.ExportedTourLog exportedLog : exportedTour.logs()) {
                TourLog logEntry = new TourLog();
                logEntry.setTourId(savedTour.getId());
                logEntry.setDateTime(exportedLog.dateTime());
                logEntry.setComment(exportedLog.comment());
                logEntry.setDifficulty(exportedLog.difficulty());
                logEntry.setTotalDistance(exportedLog.totalDistance());
                logEntry.setTotalTime(exportedLog.totalTime());
                logEntry.setRating(exportedLog.rating());
                tourLogRepository.save(logEntry);
                importedLogs++;
            }
        }

        return new TourDataExport.ImportResult(importedTours, importedLogs);
    }

    // --- Helper ---

    private void validateImportedTour(TourDataExport.ExportedTour tour) {
        if (tour == null) {
            throw new IllegalArgumentException("Imported tour must not be null");
        }
        if (isBlank(tour.name())) {
            throw new IllegalArgumentException("Imported tour name is required");
        }
        if (isBlank(tour.from())) {
            throw new IllegalArgumentException("Imported tour start location is required");
        }
        if (isBlank(tour.to())) {
            throw new IllegalArgumentException("Imported tour destination is required");
        }
        if (isBlank(tour.transportType())) {
            throw new IllegalArgumentException("Imported tour transport type is required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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

    private boolean matchesComputedAttributes(TourResponse tour, String normalizedQuery) {
        return normalize("popularity:" + tour.getPopularity()).contains(normalizedQuery)
                || normalize("childfriendliness:" + tour.getChildFriendliness()).contains(normalizedQuery);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

}
