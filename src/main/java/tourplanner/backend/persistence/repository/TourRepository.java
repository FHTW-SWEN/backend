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
              and (
                lower(coalesce(t.name, '')) like lower(concat('%', :query, '%'))
                or lower(coalesce(t.description, '')) like lower(concat('%', :query, '%'))
                or lower(coalesce(t.from_location, '')) like lower(concat('%', :query, '%'))
                or lower(coalesce(t.to_location, '')) like lower(concat('%', :query, '%'))
                or lower(coalesce(t.transport_type, '')) like lower(concat('%', :query, '%'))
              )
            """, nativeQuery = true)
    List<Tour> searchPersistedFields(@Param("userId") Long userId, @Param("query") String query);
}
