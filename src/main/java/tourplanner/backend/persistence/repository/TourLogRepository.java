/*package tourplanner.backend.persistence.repository;

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
}*/
package tourplanner.backend.persistence.repository;

import tourplanner.backend.persistence.entity.TourLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TourLogRepository extends JpaRepository<TourLog, Long> {
    List<TourLog> findByTourId(Long tourId);

    long countByTourId(Long tourId);

    @Query(value = """
            select distinct l.tour_id from tour_logs l
            where l.tour_id in (:tourIds)
              and (
                lower(coalesce(l.date_time, '')) like lower(concat('%', :query, '%'))
                or lower(coalesce(l.comment, '')) like lower(concat('%', :query, '%'))
              )
            """, nativeQuery = true)
    List<Long> findMatchingTourIds(@Param("tourIds") Collection<Long> tourIds, @Param("query") String query);
}
