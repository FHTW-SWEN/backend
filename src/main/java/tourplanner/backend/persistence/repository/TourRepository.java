package tourplanner.backend.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tourplanner.backend.persistence.entity.Tour;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository für Tours.
 * Spring Data JPA generiert automatisch alle Standard-CRUD-Methoden.
 */
@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
    List<Tour> findByUserId(Long userId);
    Optional<Tour> findByIdAndUserId(Long id, Long userId);

    @Query(value = """
            select * from tours t
            where t.user_id = :userId
              and to_tsvector('simple', concat_ws(' ',
                    coalesce(t.name, ''),
                    coalesce(t.description, ''),
                    coalesce(t.from_location, ''),
                    coalesce(t.to_location, ''),
                    coalesce(t.transport_type, ''),
                    coalesce(cast(t.distance as text), ''),
                    coalesce(cast(t.estimated_time as text), '')
                  )) @@ websearch_to_tsquery('simple', :query)
            """, nativeQuery = true)
    List<Tour> searchPersistedFields(@Param("userId") Long userId, @Param("query") String query);
}
