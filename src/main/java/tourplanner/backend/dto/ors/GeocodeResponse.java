package tourplanner.backend.dto.ors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodeResponse {

    private List<Feature> features;

    public List<Feature> getFeatures() { return features; }
    public void setFeatures(List<Feature> features) { this.features = features; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature {
        private Geometry geometry;

        public Geometry getGeometry() { return geometry; }
        public void setGeometry(Geometry geometry) { this.geometry = geometry; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Geometry {
        // ORS gibt [longitude, latitude] zurück
        private List<Double> coordinates;

        public List<Double> getCoordinates() { return coordinates; }
        public void setCoordinates(List<Double> coordinates) { this.coordinates = coordinates; }
    }
}