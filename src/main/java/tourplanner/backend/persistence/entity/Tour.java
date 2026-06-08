/*package tourplanner.backend.persistence.entity;

public class Tour {
    private Long id;
    private Double distance;
    private Integer estimatedTime;

    private String name;
    private String description;
    private String from;
    private String to;
    private String transportType;

    public Tour() {}

    public Tour(Long id, String name, String description, String from, String to, String transportType, Double distance, Integer estimatedTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.from = from;
        this.to = to;
        this.transportType = transportType;
        this.distance = distance;
        this.estimatedTime = estimatedTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getTransportType() { return transportType; }
    public void setTransportType(String transportType) { this.transportType = transportType; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public Integer getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(Integer estimatedTime) { this.estimatedTime = estimatedTime; }
}*/
package tourplanner.backend.persistence.entity;

import jakarta.persistence.*;

/**
 * JPA-Entity für eine Tour.
 * Wird von Hibernate automatisch in der PostgreSQL-Tabelle "tours" gespeichert.
 */
@Entity
@Table(name = "tours")
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "from_location", nullable = false)
    private String from;

    @Column(name = "to_location", nullable = false)
    private String to;

    @Column(name = "transport_type")
    private String transportType;

    /** Distanz in Kilometern — wird von ORS befüllt, nicht vom User. */
    private Double distance;

    /** Geschätzte Zeit in Minuten — wird von ORS befüllt, nicht vom User. */
    @Column(name = "estimated_time")
    private Integer estimatedTime;

    /**
     * Route-Koordinaten als JSON-String: [[lat,lng],[lat,lng],...]
     * Wird von ORS geholt und direkt ans Frontend weitergegeben,
     * damit Leaflet die echte Route zeichnen kann.
     */
    @Column(name = "route_coordinates", columnDefinition = "TEXT")
    private String routeCoordinates;

    /** Optionaler Pfad/URL zum Tour-Bild auf dem Filesystem. */
    @Column(name = "image_url")
    private String imageUrl;

    public Tour() {}

    // Getters & Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getTransportType() { return transportType; }
    public void setTransportType(String transportType) { this.transportType = transportType; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public Integer getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(Integer estimatedTime) { this.estimatedTime = estimatedTime; }

    public String getRouteCoordinates() { return routeCoordinates; }
    public void setRouteCoordinates(String routeCoordinates) { this.routeCoordinates = routeCoordinates; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}