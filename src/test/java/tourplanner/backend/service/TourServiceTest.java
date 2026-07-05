package tourplanner.backend.service;

import org.junit.jupiter.api.Test;
import tourplanner.backend.dto.TourDataExport;
import tourplanner.backend.dto.TourResponse;
import tourplanner.backend.persistence.entity.Tour;
import tourplanner.backend.persistence.entity.TourLog;
import tourplanner.backend.persistence.repository.TourLogRepository;
import tourplanner.backend.persistence.repository.TourRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TourServiceTest {
    private static final Long USER_ID = 1L;

    private final TourRepository tourRepository = mock(TourRepository.class);
    private final TourLogRepository tourLogRepository = mock(TourLogRepository.class);
    private final RouteService routeService = mock(RouteService.class);
    private final TourService tourService = new TourService(tourRepository, tourLogRepository, routeService);

    @Test
    void getAllTours_includesPopularityFromTourLogCount() {
        Tour tour = sampleTour(1L);
        when(tourRepository.findByUserId(USER_ID)).thenReturn(List.of(tour));
        when(tourLogRepository.countByTourId(1L)).thenReturn(2L);
        when(tourLogRepository.findByTourId(1L)).thenReturn(List.of());

        List<TourResponse> tours = tourService.getAllToursByUser(USER_ID);

        assertEquals(1, tours.size());
        assertEquals(2, tours.get(0).getPopularity());
        assertEquals(0, tours.get(0).getChildFriendliness());
    }

    @Test
    void getTourById_includesZeroPopularityWhenTourHasNoLogs() {
        Tour tour = sampleTour(1L);
        when(tourRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.of(tour));
        when(tourLogRepository.countByTourId(1L)).thenReturn(0L);
        when(tourLogRepository.findByTourId(1L)).thenReturn(List.of());

        Optional<TourResponse> response = tourService.getTourByIdAndUser(1L, USER_ID);

        assertTrue(response.isPresent());
        assertEquals(0, response.get().getPopularity());
        assertEquals(0, response.get().getChildFriendliness());
    }

    @Test
    void getTourById_returnsHighChildFriendlinessForEasyLogs() {
        Tour tour = sampleTour(1L);
        when(tourRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.of(tour));
        when(tourLogRepository.countByTourId(1L)).thenReturn(1L);
        when(tourLogRepository.findByTourId(1L))
                .thenReturn(List.of(sampleTourLog(1L, 1, 45, 2.5)));

        Optional<TourResponse> response = tourService.getTourByIdAndUser(1L, USER_ID);

        assertTrue(response.isPresent());
        assertEquals(5, response.get().getChildFriendliness());
    }

    @Test
    void getTourById_returnsLowChildFriendlinessForDifficultLongLogs() {
        Tour tour = sampleTour(1L);
        when(tourRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.of(tour));
        when(tourLogRepository.countByTourId(1L)).thenReturn(1L);
        when(tourLogRepository.findByTourId(1L))
                .thenReturn(List.of(sampleTourLog(1L, 5, 300, 20.0)));

        Optional<TourResponse> response = tourService.getTourByIdAndUser(1L, USER_ID);

        assertTrue(response.isPresent());
        assertEquals(1, response.get().getChildFriendliness());
    }

    @Test
    void getTourById_returnsRoundedAverageChildFriendlinessForMultipleLogs() {
        Tour tour = sampleTour(1L);
        when(tourRepository.findByIdAndUserId(1L, USER_ID)).thenReturn(Optional.of(tour));
        when(tourLogRepository.countByTourId(1L)).thenReturn(2L);
        when(tourLogRepository.findByTourId(1L))
                .thenReturn(List.of(
                        sampleTourLog(1L, 1, 45, 2.5),
                        sampleTourLog(1L, 5, 300, 20.0)
                ));

        Optional<TourResponse> response = tourService.getTourByIdAndUser(1L, USER_ID);

        assertTrue(response.isPresent());
        assertEquals(3, response.get().getChildFriendliness());
    }

    @Test
    void searchTours_matchesTourFields() {
        Tour matchingTour = sampleTour(1L);
        Tour otherTour = sampleTour(2L);
        otherTour.setName("City Walk");
        otherTour.setTo("Graz");
        when(tourRepository.findByUserId(USER_ID)).thenReturn(List.of(matchingTour, otherTour));
        when(tourRepository.searchPersistedFields(USER_ID, "salzburg")).thenReturn(List.of(matchingTour));
        when(tourLogRepository.findMatchingTourIds(List.of(1L, 2L), "salzburg")).thenReturn(List.of());
        when(tourLogRepository.countByTourId(1L)).thenReturn(0L);
        when(tourLogRepository.countByTourId(2L)).thenReturn(0L);
        when(tourLogRepository.findByTourId(1L)).thenReturn(List.of());
        when(tourLogRepository.findByTourId(2L)).thenReturn(List.of());

        List<TourResponse> results = tourService.searchTours("salzburg", USER_ID);

        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
    }

    @Test
    void searchTours_matchesTourLogFields() {
        Tour matchingTour = sampleTour(1L);
        Tour otherTour = sampleTour(2L);
        when(tourRepository.findByUserId(USER_ID)).thenReturn(List.of(matchingTour, otherTour));
        when(tourRepository.searchPersistedFields(USER_ID, "forest")).thenReturn(List.of());
        when(tourLogRepository.findMatchingTourIds(List.of(1L, 2L), "forest")).thenReturn(List.of(1L));
        when(tourLogRepository.countByTourId(1L)).thenReturn(1L);
        when(tourLogRepository.countByTourId(2L)).thenReturn(0L);
        when(tourLogRepository.findByTourId(1L))
                .thenReturn(List.of(sampleTourLog(1L, 2, 90, 4.0, "Great forest route")));
        when(tourLogRepository.findByTourId(2L)).thenReturn(List.of());

        List<TourResponse> results = tourService.searchTours("forest", USER_ID);

        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
    }

    @Test
    void searchTours_matchesComputedAttributes() {
        Tour matchingTour = sampleTour(1L);
        Tour otherTour = sampleTour(2L);
        when(tourRepository.findByUserId(USER_ID)).thenReturn(List.of(matchingTour, otherTour));
        when(tourRepository.searchPersistedFields(USER_ID, "popularity:7")).thenReturn(List.of());
        when(tourLogRepository.findMatchingTourIds(List.of(1L, 2L), "popularity:7")).thenReturn(List.of());
        when(tourLogRepository.countByTourId(1L)).thenReturn(7L);
        when(tourLogRepository.countByTourId(2L)).thenReturn(0L);
        when(tourLogRepository.findByTourId(1L)).thenReturn(List.of());
        when(tourLogRepository.findByTourId(2L)).thenReturn(List.of());

        List<TourResponse> results = tourService.searchTours("popularity:7", USER_ID);

        assertEquals(1, results.size());
        assertEquals(7, results.get(0).getPopularity());
    }

    @Test
    void searchTours_returnsAllToursForBlankQuery() {
        Tour firstTour = sampleTour(1L);
        Tour secondTour = sampleTour(2L);
        when(tourRepository.findByUserId(USER_ID)).thenReturn(List.of(firstTour, secondTour));
        when(tourLogRepository.countByTourId(1L)).thenReturn(0L);
        when(tourLogRepository.countByTourId(2L)).thenReturn(0L);
        when(tourLogRepository.findByTourId(1L)).thenReturn(List.of());
        when(tourLogRepository.findByTourId(2L)).thenReturn(List.of());

        List<TourResponse> results = tourService.searchTours("   ", USER_ID);

        assertEquals(2, results.size());
    }

    @Test
    void exportTourData_includesToursAndLogsForUser() {
        Tour tour = sampleTour(1L);
        TourLog log = sampleTourLog(1L, 2, 90, 4.0, "Nice trip");
        log.setDateTime("2026-06-18T12:00:00");
        log.setRating(5);
        when(tourRepository.findByUserId(USER_ID)).thenReturn(List.of(tour));
        when(tourLogRepository.findByTourId(1L)).thenReturn(List.of(log));

        TourDataExport export = tourService.exportTourData(USER_ID);

        assertEquals(1, export.tours().size());
        assertEquals("Vienna to Salzburg", export.tours().get(0).name());
        assertEquals(1, export.tours().get(0).logs().size());
        assertEquals("Nice trip", export.tours().get(0).logs().get(0).comment());
    }

    @Test
    void importTourData_savesToursForUserAndReassignsLogTourId() {
        Tour savedTour = sampleTour(99L);
        when(tourRepository.save(any(Tour.class))).thenReturn(savedTour);
        TourDataExport data = new TourDataExport(List.of(
                new TourDataExport.ExportedTour(
                        "Imported Tour",
                        "Imported description",
                        "Graz",
                        "Linz",
                        "walk",
                        12.0,
                        180,
                        "[[47.0,15.0]]",
                        List.of(new TourDataExport.ExportedTourLog(
                                "2026-06-18T12:00:00",
                                "Imported log",
                                3,
                                11.5,
                                170,
                                4
                        ))
                )
        ));

        TourDataExport.ImportResult result = tourService.importTourData(data, USER_ID);

        assertEquals(1, result.importedTours());
        assertEquals(1, result.importedLogs());
        verify(tourRepository).save(any(Tour.class));
        verify(tourLogRepository).save(any(TourLog.class));
    }

    private Tour sampleTour(Long id) {
        Tour tour = new Tour();
        tour.setId(id);
        tour.setUserId(USER_ID);
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
        return sampleTourLog(tourId, difficulty, totalTime, totalDistance, null);
    }

    private TourLog sampleTourLog(Long tourId, Integer difficulty, Integer totalTime, Double totalDistance, String comment) {
        TourLog tourLog = new TourLog();
        tourLog.setTourId(tourId);
        tourLog.setComment(comment);
        tourLog.setDifficulty(difficulty);
        tourLog.setTotalTime(totalTime);
        tourLog.setTotalDistance(totalDistance);
        return tourLog;
    }
}
