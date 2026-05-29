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

import tourplanner.backend.dto.TourResponse;
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

    public List<TourResponse> getAllTours() {
        return tourRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<TourResponse> getTourById(Long id) {
        return tourRepository.findById(id)
                .map(this::toResponse);
    }

    public Tour createTour(Tour tour) {
        tour.setId(null); // ensure DB generates the ID
        return tourRepository.save(tour);
    }

    public Optional<Tour> updateTour(Long id, Tour updated) {
        return tourRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setDescription(updated.getDescription());
            existing.setFrom(updated.getFrom());
            existing.setTo(updated.getTo());
            existing.setTransportType(updated.getTransportType());
            existing.setDistance(updated.getDistance());
            existing.setEstimatedTime(updated.getEstimatedTime());
            existing.setImageUrl(updated.getImageUrl());
            return tourRepository.save(existing);
        });
    }

    public boolean deleteTour(Long id) {
        if (!tourRepository.existsById(id)) return false;
        tourRepository.deleteById(id);
        return true;
    }

    private TourResponse toResponse(Tour tour) {
        return new TourResponse(tour, 0, 0);
    }
}
