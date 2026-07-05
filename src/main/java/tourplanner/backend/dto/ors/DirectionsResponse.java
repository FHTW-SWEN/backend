package tourplanner.backend.dto.ors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Mapped the GeoJSON Response from ORS /v2/directions/{profile}/geojson
 *
 * ORS Response Structure:
 * {
 *   "type": "FeatureCollection",
 *   "features": [{
 *     "type": "Feature",
 *     "geometry": { "type": "LineString", "coordinates": [[lng,lat], ...] },
 *     "properties": { "summary": { "distance": 12345.6, "duration": 3600.0 } }
 *   }]
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectionsResponse {

    private List<Feature> features;

    public List<Feature> getFeatures() { return features; }
    public void setFeatures(List<Feature> features) { this.features = features; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feature {
        private Geometry geometry;
        private Properties properties;

        public Geometry getGeometry() { return geometry; }
        public void setGeometry(Geometry geometry) { this.geometry = geometry; }

        public Properties getProperties() { return properties; }
        public void setProperties(Properties properties) { this.properties = properties; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Geometry {
        // ORS returns [longitude, latitude]
        private List<List<Double>> coordinates;

        public List<List<Double>> getCoordinates() { return coordinates; }
        public void setCoordinates(List<List<Double>> coordinates) { this.coordinates = coordinates; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        private Summary summary;

        public Summary getSummary() { return summary; }
        public void setSummary(Summary summary) { this.summary = summary; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Summary {
        // Distance in meters
        private double distance;
        // Duration in sec
        private double duration;

        public double getDistance() { return distance; }
        public void setDistance(double distance) { this.distance = distance; }

        public double getDuration() { return duration; }
        public void setDuration(double duration) { this.duration = duration; }
    }
}
