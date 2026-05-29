package tourplanner.backend.service;

import org.junit.jupiter.api.Test;
import tourplanner.backend.dto.TourResponse;
import tourplanner.backend.persistence.entity.Tour;
import tourplanner.backend.persistence.repository.TourLogRepository;
import tourplanner.backend.persistence.repository.TourRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TourServiceTest {

    private final TourRepository tourRepository = mock(TourRepository.class);
    private final TourLogRepository tourLogRepository = mock(TourLogRepository.class);
    private final TourService tourService = new TourService(tourRepository, tourLogRepository);

    @Test
    void getAllTours_includesPopularityFromTourLogCount() {
        Tour tour = sampleTour(1L);
        when(tourRepository.findAll()).thenReturn(List.of(tour));
        when(tourLogRepository.countByTourId(1L)).thenReturn(2L);

        List<TourResponse> tours = tourService.getAllTours();

        assertEquals(1, tours.size());
        assertEquals(2, tours.get(0).getPopularity());
        assertEquals(0, tours.get(0).getChildFriendliness());
    }

    @Test
    void getTourById_includesZeroPopularityWhenTourHasNoLogs() {
        Tour tour = sampleTour(1L);
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourLogRepository.countByTourId(1L)).thenReturn(0L);

        Optional<TourResponse> response = tourService.getTourById(1L);

        assertTrue(response.isPresent());
        assertEquals(0, response.get().getPopularity());
        assertEquals(0, response.get().getChildFriendliness());
    }

    private Tour sampleTour(Long id) {
        Tour tour = new Tour();
        tour.setId(id);
        tour.setName("Vienna to Salzburg");
        tour.setDescription("A scenic bike tour");
        tour.setFrom("Vienna");
        tour.setTo("Salzburg");
        tour.setTransportType("BIKE");
        tour.setDistance(295.5);
        tour.setEstimatedTime(1200);
        return tour;
    }
}
