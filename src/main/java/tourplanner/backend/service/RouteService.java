package tourplanner.backend.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import tourplanner.backend.dto.ors.DirectionsResponse;
import tourplanner.backend.dto.ors.RouteInfo;
import tourplanner.backend.service.client.OrsClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private static final Logger log = LogManager.getLogger(RouteService.class);
    private final OrsClient orsClient;

    public RouteService(OrsClient orsClient) {
        this.orsClient = orsClient;
    }

    public RouteInfo calculateRoute(String from, String to, String transportType) {
        log.info("Calculate the Route: {} -> {}, Transport: {}", from, to, transportType);

        double[] fromCoords = orsClient.geocode(from);
        double[] toCoords = orsClient.geocode(to);

        if (fromCoords == null) throw new IllegalArgumentException("Starting location not found: " + from);
        if (toCoords == null) throw new IllegalArgumentException("Destination not found: " + to);

        String profile = toOrsProfile(transportType);
        DirectionsResponse directions = orsClient.getDirections(fromCoords, toCoords, profile);

        // GeoJSON: features[0].properties.summary
        DirectionsResponse.Feature feature = directions.getFeatures().get(0);
        double distanceKm = Math.round(feature.getProperties().getSummary().getDistance() / 10.0) / 100.0;
        int estimatedTimeMinutes = (int) Math.round(feature.getProperties().getSummary().getDuration() / 60.0);

        // GeoJSON: features[0].geometry.coordinates = [[lng,lat], ...]
        String routeCoordinatesJson = buildCoordinatesJson(feature.getGeometry().getCoordinates());

        log.info("Route calculated: {}km, {}min", distanceKm, estimatedTimeMinutes);
        return new RouteInfo(distanceKm, estimatedTimeMinutes, routeCoordinatesJson);
    }

    private String toOrsProfile(String transportType) {
        if (transportType == null) return "foot-walking";
        return switch (transportType.toLowerCase()) {
            case "car", "auto" -> "driving-car";
            case "bike", "cycling", "fahrrad" -> "cycling-regular";
            default -> "foot-walking";
        };
    }

    private String buildCoordinatesJson(List<List<Double>> orsCoords) {
        String inner = orsCoords.stream()
                .map(coord -> "[" + coord.get(1) + "," + coord.get(0) + "]")
                .collect(Collectors.joining(","));
        return "[" + inner + "]";
    }
}

