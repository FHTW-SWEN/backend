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
import tourplanner.backend.dto.ors.RouteInfo;
import tourplanner.backend.persistence.entity.Tour;
import tourplanner.backend.persistence.repository.TourRepository;

import java.util.List;
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
    private final RouteService routeService;

    public TourService(TourRepository tourRepository, RouteService routeService) {
        this.tourRepository = tourRepository;
        this.routeService = routeService;
    }

    public List<Tour> getAllTours() {
        log.debug("Lade alle Tours aus der Datenbank");
        return tourRepository.findAll();
    }

    public Optional<Tour> getTourById(Long id) {
        log.debug("Suche Tour mit ID: {}", id);
        return tourRepository.findById(id);
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
}