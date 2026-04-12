package tourplanner.backend.service;

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
}