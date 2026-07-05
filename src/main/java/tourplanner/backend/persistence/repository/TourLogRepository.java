package tourplanner.backend.persistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tourplanner.backend.persistence.entity.TourLog;
import org.springframework.data.jpa.repository.JpaRepository;
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
              and to_tsvector('simple', concat_ws(' ',
                    coalesce(l.date_time, ''),
                    coalesce(l.comment, ''),
                    coalesce(cast(l.difficulty as text), ''),
                    coalesce(cast(l.total_distance as text), ''),
                    coalesce(cast(l.total_time as text), ''),
                    coalesce(cast(l.rating as text), '')
                  )) @@ websearch_to_tsquery('simple', :query)
            """, nativeQuery = true)
    List<Long> findMatchingTourIds(@Param("tourIds") Collection<Long> tourIds, @Param("query") String query);
}
