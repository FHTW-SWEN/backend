/*package tourplanner.backend.service;

import tourplanner.backend.dto.TourResponse;
import tourplanner.backend.persistence.entity.Tour;
import tourplanner.backend.persistence.entity.TourLog;
import tourplanner.backend.persistence.repository.TourLogRepository;
import tourplanner.backend.persistence.repository.TourRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TourService {

    private final TourRepository tourRepository;
    private final TourLogRepository tourLogRepository;

    public TourService(TourRepository tourRepository, TourLogRepository tourLogRepository) {
        this.tourRepository = tourRepository;
        this.tourLogRepository = tourLogRepository;
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
        int popularity = Math.toIntExact(tourLogRepository.countByTourId(tour.getId()));
        int childFriendliness = calculateChildFriendliness(tour.getId());
        return new TourResponse(tour, popularity, childFriendliness);
    }

    private int calculateChildFriendliness(Long tourId) {
        List<TourLog> logs = tourLogRepository.findByTourId(tourId);
        if (logs.isEmpty()) {
            return 0;
        }

        double averageScore = logs.stream()
                .mapToDouble(this::calculateLogChildFriendliness)
                .average()
                .orElse(0);
        return (int) Math.round(averageScore);
    }

    private double calculateLogChildFriendliness(TourLog log) {
        return (scoreDifficulty(log.getDifficulty())
                + scoreTotalTime(log.getTotalTime())
                + scoreTotalDistance(log.getTotalDistance())) / 3.0;
    }

    private int scoreDifficulty(Integer difficulty) {
        if (difficulty == null) {
            return 1;
        }
        return Math.max(1, Math.min(5, 6 - difficulty));
    }

    private int scoreTotalTime(Integer totalTime) {
        if (totalTime == null) {
            return 1;
        }
        if (totalTime <= 60) return 5;
        if (totalTime <= 120) return 4;
        if (totalTime <= 180) return 3;
        if (totalTime <= 240) return 2;
        return 1;
    }

    private int scoreTotalDistance(Double totalDistance) {
        if (totalDistance == null) {
            return 1;
        }
        if (totalDistance <= 3) return 5;
        if (totalDistance <= 5) return 4;
        if (totalDistance <= 10) return 3;
        if (totalDistance <= 15) return 2;
        return 1;
    }
}*/
package tourplanner.backend.service;

import tourplanner.backend.dto.TourResponse;
import tourplanner.backend.persistence.entity.Tour;
import tourplanner.backend.persistence.entity.TourLog;
import tourplanner.backend.persistence.repository.TourLogRepository;
import tourplanner.backend.persistence.repository.TourRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TourService {

    private final TourRepository tourRepository;
    private final TourLogRepository tourLogRepository;

    public TourService(TourRepository tourRepository, TourLogRepository tourLogRepository) {
        this.tourRepository = tourRepository;
        this.tourLogRepository = tourLogRepository;
    }

    // Returns only tours belonging to the given user
    public List<TourResponse> getAllToursByUser(Long userId) {
        return tourRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<TourResponse> getTourByIdAndUser(Long id, Long userId) {
        return tourRepository.findByIdAndUserId(id, userId)
                .map(this::toResponse);
    }

    public Tour createTour(Tour tour) {
        tour.setId(null);
        return tourRepository.save(tour);
    }

    public Optional<Tour> updateTourForUser(Long id, Tour updated, Long userId) {
        return tourRepository.findByIdAndUserId(id, userId).map(existing -> {
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

    public boolean deleteTourForUser(Long id, Long userId) {
        return tourRepository.findByIdAndUserId(id, userId).map(tour -> {
            tourRepository.delete(tour);
            return true;
        }).orElse(false);
    }

    // Keep old methods for backwards compatibility
    public List<TourResponse> getAllTours() {
        return tourRepository.findAll().stream().map(this::toResponse).toList();
    }

    public Optional<TourResponse> getTourById(Long id) {
        return tourRepository.findById(id).map(this::toResponse);
    }

    public Optional<Tour> updateTour(Long id, Tour updated) {
        return updateTourForUser(id, updated, updated.getUserId());
    }

    public boolean deleteTour(Long id) {
        if (!tourRepository.existsById(id)) return false;
        tourRepository.deleteById(id);
        return true;
    }

    private TourResponse toResponse(Tour tour) {
        int popularity = Math.toIntExact(tourLogRepository.countByTourId(tour.getId()));
        int childFriendliness = calculateChildFriendliness(tour.getId());
        return new TourResponse(tour, popularity, childFriendliness);
    }

    private int calculateChildFriendliness(Long tourId) {
        List<TourLog> logs = tourLogRepository.findByTourId(tourId);
        if (logs.isEmpty()) return 0;

        double averageScore = logs.stream()
                .mapToDouble(this::calculateLogChildFriendliness)
                .average()
                .orElse(0);
        return (int) Math.round(averageScore);
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
}
