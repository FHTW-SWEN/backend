package tourplanner.backend.dto;

import tourplanner.backend.persistence.entity.Tour;

public class TourResponse {
    private final Long id;
    private final String name;
    private final String description;
    private final String from;
    private final String to;
    private final String transportType;
    private final Double distance;
    private final Integer estimatedTime;
    private final String imageUrl;
    private final int popularity;
    private final int childFriendliness;

    public TourResponse(Tour tour, int popularity, int childFriendliness) {
        this.id = tour.getId();
        this.name = tour.getName();
        this.description = tour.getDescription();
        this.from = tour.getFrom();
        this.to = tour.getTo();
        this.transportType = tour.getTransportType();
        this.distance = tour.getDistance();
        this.estimatedTime = tour.getEstimatedTime();
        this.imageUrl = tour.getImageUrl();
        this.popularity = popularity;
        this.childFriendliness = childFriendliness;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getTransportType() { return transportType; }
    public Double getDistance() { return distance; }
    public Integer getEstimatedTime() { return estimatedTime; }
    public String getImageUrl() { return imageUrl; }
    public int getPopularity() { return popularity; }
    public int getChildFriendliness() { return childFriendliness; }
}
