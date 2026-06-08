package tourplanner.backend.service;

import org.junit.jupiter.api.Test;
import tourplanner.backend.dto.TourResponse;
import tourplanner.backend.persistence.entity.Tour;
import tourplanner.backend.persistence.entity.TourLog;
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
        when(tourLogRepository.findByTourId(1L)).thenReturn(List.of());

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
        when(tourLogRepository.findByTourId(1L)).thenReturn(List.of());

        Optional<TourResponse> response = tourService.getTourById(1L);

        assertTrue(response.isPresent());
        assertEquals(0, response.get().getPopularity());
        assertEquals(0, response.get().getChildFriendliness());
    }

    @Test
    void getTourById_returnsHighChildFriendlinessForEasyLogs() {
        Tour tour = sampleTour(1L);
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourLogRepository.countByTourId(1L)).thenReturn(1L);
        when(tourLogRepository.findByTourId(1L))
                .thenReturn(List.of(sampleTourLog(1L, 1, 45, 2.5)));

        Optional<TourResponse> response = tourService.getTourById(1L);

        assertTrue(response.isPresent());
        assertEquals(5, response.get().getChildFriendliness());
    }

    @Test
    void getTourById_returnsLowChildFriendlinessForDifficultLongLogs() {
        Tour tour = sampleTour(1L);
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourLogRepository.countByTourId(1L)).thenReturn(1L);
        when(tourLogRepository.findByTourId(1L))
                .thenReturn(List.of(sampleTourLog(1L, 5, 300, 20.0)));

        Optional<TourResponse> response = tourService.getTourById(1L);

        assertTrue(response.isPresent());
        assertEquals(1, response.get().getChildFriendliness());
    }

    @Test
    void getTourById_returnsRoundedAverageChildFriendlinessForMultipleLogs() {
        Tour tour = sampleTour(1L);
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourLogRepository.countByTourId(1L)).thenReturn(2L);
        when(tourLogRepository.findByTourId(1L))
                .thenReturn(List.of(
                        sampleTourLog(1L, 1, 45, 2.5),
                        sampleTourLog(1L, 5, 300, 20.0)
                ));

        Optional<TourResponse> response = tourService.getTourById(1L);

        assertTrue(response.isPresent());
        assertEquals(3, response.get().getChildFriendliness());
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

    private TourLog sampleTourLog(Long tourId, Integer difficulty, Integer totalTime, Double totalDistance) {
        TourLog tourLog = new TourLog();
        tourLog.setTourId(tourId);
        tourLog.setDifficulty(difficulty);
        tourLog.setTotalTime(totalTime);
        tourLog.setTotalDistance(totalDistance);
        return tourLog;
    }
}
