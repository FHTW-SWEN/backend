package tourplanner.backend.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tourplanner.backend.persistence.entity.TourPhoto;

import java.util.List;

@Repository
public interface TourPhotoRepository extends JpaRepository<TourPhoto, Long> {
    List<TourPhoto> findByTourIdOrderByUploadedAtDesc(Long tourId);
    long countByTourId(Long tourId);
}
