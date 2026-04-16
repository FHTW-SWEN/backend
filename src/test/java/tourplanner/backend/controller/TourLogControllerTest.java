package tourplanner.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import tourplanner.backend.persistence.entity.TourLog;
import tourplanner.backend.service.TourLogService;
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

@WebMvcTest(TourLogController.class)
class TourLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TourLogService tourLogService;

    private TourLog sampleTourLog() {
        TourLog tourLog = new TourLog();
        tourLog.setId(1L);
        tourLog.setTourId(10L);
        tourLog.setDateTime("2026-04-16T10:15:00");
        tourLog.setComment("Nice weather and easy ride");
        tourLog.setDifficulty(2);
        tourLog.setTotalTime(180);
        tourLog.setRating(5);
        return tourLog;
    }

    @Test
    void getAllTourLogs_returnsList() throws Exception {
        when(tourLogService.getAllTourLogs()).thenReturn(List.of(sampleTourLog()));

        mockMvc.perform(get("/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tourId").value(10))
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test
    void getTourLogsByTourId_returnsFilteredList() throws Exception {
        when(tourLogService.getTourLogsByTourId(10L)).thenReturn(List.of(sampleTourLog()));

        mockMvc.perform(get("/logs").param("tourId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tourId").value(10));
    }

    @Test
    void getTourLogById_returnsOk() throws Exception {
        when(tourLogService.getTourLogById(1L)).thenReturn(Optional.of(sampleTourLog()));

        mockMvc.perform(get("/logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("Nice weather and easy ride"));
    }

    @Test
    void getTourLogById_returnsNotFound() throws Exception {
        when(tourLogService.getTourLogById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/logs/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTourLog_returnsCreated() throws Exception {
        when(tourLogService.createTourLog(any())).thenReturn(sampleTourLog());

        mockMvc.perform(post("/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTourLog())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateTourLog_returnsOk() throws Exception {
        when(tourLogService.updateTourLog(eq(1L), any())).thenReturn(Optional.of(sampleTourLog()));

        mockMvc.perform(put("/logs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTourLog())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.difficulty").value(2));
    }

    @Test
    void updateTourLog_returnsNotFound() throws Exception {
        when(tourLogService.updateTourLog(eq(999L), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/logs/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTourLog())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTourLog_returnsNoContent() throws Exception {
        when(tourLogService.deleteTourLog(1L)).thenReturn(true);

        mockMvc.perform(delete("/logs/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTourLog_returnsNotFound() throws Exception {
        when(tourLogService.deleteTourLog(999L)).thenReturn(false);

        mockMvc.perform(delete("/logs/999"))
                .andExpect(status().isNotFound());
    }
}
