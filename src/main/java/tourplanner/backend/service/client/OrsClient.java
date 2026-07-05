package tourplanner.backend.service.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tourplanner.backend.dto.ors.DirectionsResponse;
import tourplanner.backend.dto.ors.GeocodeResponse;

import java.util.List;
import java.util.Map;

@Component
public class OrsClient {

    private static final Logger log = LogManager.getLogger(OrsClient.class);
    private static final String ORS_BASE_URL = "https://api.openrouteservice.org";

    private final WebClient webClient;
    private final String apiKey;

    public OrsClient(@Value("${openrouteservice.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(ORS_BASE_URL)
                .defaultHeader("Authorization", apiKey)
                .build();
        log.info("OrsClient initializes with the ORS Base URL: {}", ORS_BASE_URL);
    }

    public double[] geocode(String placeName) {
        log.debug("Geocode Location: {}", placeName);

        GeocodeResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/geocode/search")
                        .queryParam("api_key", apiKey)
                        .queryParam("text", placeName)
                        .queryParam("size", 1)
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .doOnNext(body -> log.error("ORS Geocode Error: {}", body))
                                .map(body -> new RuntimeException("ORS Geocode Error: " + body))
                )
                .bodyToMono(GeocodeResponse.class)
                .block();

        if (response == null || response.getFeatures() == null || response.getFeatures().isEmpty()) {
            log.warn("No Geocoding Result for: {}", placeName);
            return null;
        }

        List<Double> coords = response.getFeatures().get(0).getGeometry().getCoordinates();
        double lat = coords.get(1);
        double lng = coords.get(0);

        log.debug("Geocode for '{}': lat={}, lng={}", placeName, lat, lng);
        return new double[]{lat, lng};
    }

    public DirectionsResponse getDirections(double[] fromCoords, double[] toCoords, String profile) {
        log.debug("Get Route: Profile={}", profile);

        // ORS expects [longitude, latitude]
        List<List<Double>> coordinates = List.of(
                List.of(fromCoords[1], fromCoords[0]),
                List.of(toCoords[1], toCoords[0])
        );

        Map<String, Object> body = Map.of("coordinates", coordinates);

        DirectionsResponse response = webClient.post()
                .uri("/v2/directions/" + profile + "/geojson")
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .doOnNext(b -> log.error("ORS Directions Error: {}", b))
                                .map(b -> new RuntimeException("ORS Error: " + b))
                )
                .bodyToMono(DirectionsResponse.class)
                .block();

        if (response == null || response.getFeatures() == null || response.getFeatures().isEmpty()) {
            log.error("No route received from ORS for this profile: {}", profile);
            throw new IllegalStateException("ORS did not return a route.");
        }

        log.debug("Get Route: {}m, {}s",
                response.getFeatures().get(0).getProperties().getSummary().getDistance(),
                response.getFeatures().get(0).getProperties().getSummary().getDuration());

        return response;
    }
}

