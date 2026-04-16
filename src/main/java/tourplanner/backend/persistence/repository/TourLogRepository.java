package tourplanner.backend.persistence.repository;

import tourplanner.backend.persistence.entity.TourLog;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TourLogRepository {

    private final List<TourLog> tourLogs = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<TourLog> findAll() {
        return tourLogs;
    }

    public List<TourLog> findByTourId(Long tourId) {
        return tourLogs.stream()
                .filter(log -> log.getTourId().equals(tourId))
                .toList();
    }

    public Optional<TourLog> findById(Long id) {
        return tourLogs.stream()
                .filter(log -> log.getId().equals(id))
                .findFirst();
    }

    public TourLog save(TourLog tourLog) {
        tourLog.setId(idCounter.getAndIncrement());
        tourLogs.add(tourLog);
        return tourLog;
    }

    public Optional<TourLog> update(Long id, TourLog updated) {
        return findById(id).map(tourLog -> {
            tourLog.setTourId(updated.getTourId());
            tourLog.setDateTime(updated.getDateTime());
            tourLog.setComment(updated.getComment());
            tourLog.setDifficulty(updated.getDifficulty());
            tourLog.setTotalTime(updated.getTotalTime());
            tourLog.setRating(updated.getRating());
            return tourLog;
        });
    }

    public boolean delete(Long id) {
        return tourLogs.removeIf(log -> log.getId().equals(id));
    }
}
