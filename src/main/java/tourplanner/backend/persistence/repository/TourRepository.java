/*package tourplanner.backend.persistence.repository;

import tourplanner.backend.persistence.entity.Tour;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TourRepository {

    private final List<Tour> tours = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<Tour> findAll() {
        return tours;
    }

    public Optional<Tour> findById(Long id) {
        return tours.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
    }

    public Tour save(Tour tour) {
        tour.setId(Long.valueOf(idCounter.getAndIncrement()));
        tours.add(tour);
        return tour;
    }

    public Optional<Tour> update(Long id, Tour updated) {
        return findById(id).map(tour -> {
            tour.setName(updated.getName());
            tour.setDescription(updated.getDescription());
            tour.setFrom(updated.getFrom());
            tour.setTo(updated.getTo());
            tour.setTransportType(updated.getTransportType());
            tour.setDistance(updated.getDistance());
            tour.setEstimatedTime(updated.getEstimatedTime());
            return tour;
        });
    }

    public boolean delete(Long id) {
        return tours.removeIf(t -> t.getId().equals(id));
    }
}*/
package tourplanner.backend.persistence.repository;

import tourplanner.backend.persistence.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
    List<Tour> findByUserId(Long userId);
    Optional<Tour> findByIdAndUserId(Long id, Long userId);
}
