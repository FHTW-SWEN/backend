package tourplanner.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import tourplanner.backend.dto.TourDataExport;
import tourplanner.backend.dto.TourResponse;
import tourplanner.backend.persistence.entity.Tour;
import tourplanner.backend.service.JwtService;
import tourplanner.backend.service.TourService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TourController.class)
@AutoConfigureMockMvc(addFilters = false)
class TourControllerTest {
    private static final Long USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TourService tourService;

    @MockitoBean
    private JwtService jwtService;

    private Tour sampleTour() {
        Tour tour = new Tour();
        tour.setId(1L);
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

    private TourResponse sampleTourResponse() {
        return new TourResponse(sampleTour(), 0, 0);
    }

    // GET /api/tours
    @Test
    void getAllTours_returnsList() throws Exception {
        when(tourService.getAllToursByUser(USER_ID)).thenReturn(List.of(sampleTourResponse()));

        mockMvc.perform(get("/api/tours").requestAttr("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Vienna to Salzburg"))
                .andExpect(jsonPath("$[0].popularity").value(0))
                .andExpect(jsonPath("$[0].childFriendliness").value(0));
    }

    // GET /api/tours - empty list
    @Test
    void getAllTours_returnsEmptyList() throws Exception {
        when(tourService.getAllToursByUser(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/tours").requestAttr("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // GET /api/tours/search?q=...
    @Test
    void searchTours_returnsMatchingList() throws Exception {
        when(tourService.searchTours("salzburg", USER_ID)).thenReturn(List.of(sampleTourResponse()));

        mockMvc.perform(get("/api/tours/search").param("q", "salzburg").requestAttr("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Vienna to Salzburg"));
    }

    @Test
    void exportTours_returnsUserTourData() throws Exception {
        TourDataExport export = new TourDataExport(List.of(
                new TourDataExport.ExportedTour(
                        "Vienna to Salzburg",
                        "A scenic bike tour",
                        "Vienna",
                        "Salzburg",
                        "BIKE",
                        295.5,
                        1200,
                        "[[48.2,16.3]]",
                        "/images/tour.jpg",
                        List.of()
                )
        ));
        when(tourService.exportTourData(USER_ID)).thenReturn(export);

        mockMvc.perform(get("/api/tours/export").requestAttr("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tours[0].name").value("Vienna to Salzburg"))
                .andExpect(jsonPath("$.tours[0].routeCoordinates").value("[[48.2,16.3]]"));
    }

    @Test
    void importTours_returnsImportResult() throws Exception {
        TourDataExport data = new TourDataExport(List.of());
        when(tourService.importTourData(any(), eq(USER_ID)))
                .thenReturn(new TourDataExport.ImportResult(2, 3));

        mockMvc.perform(post("/api/tours/import")
                        .requestAttr("userId", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.importedTours").value(2))
                .andExpect(jsonPath("$.importedLogs").value(3));
    }

    // GET /api/tours/{id}
    @Test
    void getTourById_returnsOk() throws Exception {
        when(tourService.getTourByIdAndUser(1L, USER_ID)).thenReturn(Optional.of(sampleTourResponse()));

        mockMvc.perform(get("/api/tours/1").requestAttr("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Vienna to Salzburg"))
                .andExpect(jsonPath("$.popularity").value(0))
                .andExpect(jsonPath("$.childFriendliness").value(0));
    }

    // GET /api/tours/{id} - not found (edge case)
    @Test
    void getTourById_returnsNotFound() throws Exception {
        when(tourService.getTourByIdAndUser(999L, USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tours/999").requestAttr("userId", USER_ID))
                .andExpect(status().isNotFound());
    }

    // POST /api/tours
    @Test
    void createTour_returnsCreated() throws Exception {
        when(tourService.createTour(any())).thenReturn(sampleTour());

        mockMvc.perform(post("/api/tours")
                        .requestAttr("userId", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTour())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    // PUT /api/tours/{id}
    @Test
    void updateTour_returnsOk() throws Exception {
        when(tourService.updateTourForUser(eq(1L), any(), eq(USER_ID))).thenReturn(Optional.of(sampleTour()));

        mockMvc.perform(put("/api/tours/1")
                        .requestAttr("userId", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTour())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Vienna to Salzburg"));
    }

    // PUT /api/tours/{id} - not found (edge case)
    @Test
    void updateTour_returnsNotFound() throws Exception {
        when(tourService.updateTourForUser(eq(999L), any(), eq(USER_ID))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/tours/999")
                        .requestAttr("userId", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTour())))
                .andExpect(status().isNotFound());
    }

    // DELETE /api/tours/{id}
    @Test
    void deleteTour_returnsNoContent() throws Exception {
        when(tourService.deleteTourForUser(1L, USER_ID)).thenReturn(true);

        mockMvc.perform(delete("/api/tours/1").requestAttr("userId", USER_ID))
                .andExpect(status().isNoContent());
    }

    // DELETE /api/tours/{id} - not found (edge case)
    @Test
    void deleteTour_returnsNotFound() throws Exception {
        when(tourService.deleteTourForUser(999L, USER_ID)).thenReturn(false);

        mockMvc.perform(delete("/api/tours/999").requestAttr("userId", USER_ID))
                .andExpect(status().isNotFound());
    }
}
