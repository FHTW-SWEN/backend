package tourplanner.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import tourplanner.backend.persistence.entity.Tour;
import tourplanner.backend.service.TourService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
class TourControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TourService tourService;

    private Tour sampleTour() {
        Tour tour = new Tour();
        tour.setId(1L);
        tour.setName("Vienna to Salzburg");
        tour.setDescription("A scenic bike tour");
        tour.setFrom("Vienna");
        tour.setTo("Salzburg");
        tour.setTransportType("BIKE");
        tour.setDistance(295.5);
        tour.setEstimatedTime(1200);
        return tour;
    }

    // GET /tours
    @Test
    void getAllTours_returnsList() throws Exception {
        when(tourService.getAllTours()).thenReturn(List.of(sampleTour()));

        mockMvc.perform(get("/tours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Vienna to Salzburg"));
    }

    // GET /tours - empty list
    @Test
    void getAllTours_returnsEmptyList() throws Exception {
        when(tourService.getAllTours()).thenReturn(List.of());

        mockMvc.perform(get("/tours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // GET /tours/{id}
    @Test
    void getTourById_returnsOk() throws Exception {
        when(tourService.getTourById(1L)).thenReturn(Optional.of(sampleTour()));

        mockMvc.perform(get("/tours/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Vienna to Salzburg"));
    }

    // GET /tours/{id} - not found (edge case)
    @Test
    void getTourById_returnsNotFound() throws Exception {
        when(tourService.getTourById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/tours/999"))
                .andExpect(status().isNotFound());
    }

    // POST /tours
    @Test
    void createTour_returnsCreated() throws Exception {
        when(tourService.createTour(any())).thenReturn(sampleTour());

        mockMvc.perform(post("/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTour())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    // PUT /tours/{id}
    @Test
    void updateTour_returnsOk() throws Exception {
        when(tourService.updateTour(eq(1L), any())).thenReturn(Optional.of(sampleTour()));

        mockMvc.perform(put("/tours/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTour())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Vienna to Salzburg"));
    }

    // PUT /tours/{id} - not found (edge case)
    @Test
    void updateTour_returnsNotFound() throws Exception {
        when(tourService.updateTour(eq(999L), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/tours/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTour())))
                .andExpect(status().isNotFound());
    }

    // DELETE /tours/{id}
    @Test
    void deleteTour_returnsNoContent() throws Exception {
        when(tourService.deleteTour(1L)).thenReturn(true);

        mockMvc.perform(delete("/tours/1"))
                .andExpect(status().isNoContent());
    }

    // DELETE /tours/{id} - not found (edge case)
    @Test
    void deleteTour_returnsNotFound() throws Exception {
        when(tourService.deleteTour(999L)).thenReturn(false);

        mockMvc.perform(delete("/tours/999"))
                .andExpect(status().isNotFound());
    }
}