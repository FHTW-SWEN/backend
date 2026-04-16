package tourplanner.backend.service;

import tourplanner.backend.persistence.entity.TourLog;
import tourplanner.backend.persistence.repository.TourLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TourLogService {

    private final TourLogRepository tourLogRepository;

    public TourLogService(TourLogRepository tourLogRepository) {
        this.tourLogRepository = tourLogRepository;
    }

    public List<TourLog> getAllTourLogs() {
        return tourLogRepository.findAll();
    }

    public List<TourLog> getTourLogsByTourId(Long tourId) {
        return tourLogRepository.findByTourId(tourId);
    }

    public Optional<TourLog> getTourLogById(Long id) {
        return tourLogRepository.findById(id);
    }

    public TourLog createTourLog(TourLog tourLog) {
        return tourLogRepository.save(tourLog);
    }

    public Optional<TourLog> updateTourLog(Long id, TourLog tourLog) {
        return tourLogRepository.update(id, tourLog);
    }

    public boolean deleteTourLog(Long id) {
        return tourLogRepository.delete(id);
    }
}
