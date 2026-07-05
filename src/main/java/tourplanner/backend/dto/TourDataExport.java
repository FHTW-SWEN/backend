package tourplanner.backend.dto;

import java.util.List;

public record TourDataExport(List<ExportedTour> tours) {

    public record ExportedTour(
            String name,
            String description,
            String from,
            String to,
            String transportType,
            Double distance,
            Integer estimatedTime,
            String routeCoordinates,
            List<ExportedTourLog> logs
    ) {}

    public record ExportedTourLog(
            String dateTime,
            String comment,
            Integer difficulty,
            Double totalDistance,
            Integer totalTime,
            Integer rating
    ) {}

    public record ImportResult(int importedTours, int importedLogs) {}
}
